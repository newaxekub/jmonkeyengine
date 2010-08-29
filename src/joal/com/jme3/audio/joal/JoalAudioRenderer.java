package com.jme3.audio.joal;

import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioNode.Status;
import com.jme3.audio.AudioStream;
//import com.jme3.audio.DirectionalAudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.LowPassFilter;
//import com.jme3.audio.PointAudioSource;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;
import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
import net.java.games.joal.ALFactory;

/**
 * Implementation of the AudioRenderer for JOAL.
 * Supports reverb effects with environments.
 * @author Kirill
 */
public class JoalAudioRenderer implements AudioRenderer {

    private static final Logger logger = Logger.getLogger(JoalAudioRenderer.class.getName());

    private static final int BUFFER_SIZE = 16384;
    private static final int STREAMING_BUFFER_COUNT = 2;

    private AL al;
    private ALC alc;
    private ALCdevice device;
    private ALCcontext context;

    private final IntBuffer ib = BufferUtils.createIntBuffer(16);
    private final FloatBuffer fb = BufferUtils.createVector3Buffer(2);
    private final ByteBuffer nativeBuf = BufferUtils.createByteBuffer(BUFFER_SIZE);
    private final byte[] arrayBuf = new byte[BUFFER_SIZE];

    /**
     * The listener. The location, direction, and up vector is used to place the
     * listener inside the audio environment.
     */
    private Listener listener;

    /**
     * Channels used for instanced source playback.
     * A channel is an OpenAL source id that is allocated
     * dynamically to an AudioSource for instanced play with
     * the playSourceInstance method.
     */
    private int[] channels = new int[16];

    /**
     * Mapping of channels to the sources currently attached to them.
     */
    private AudioNode[] chanSrcs = new AudioNode[16];

    /**
     * The next (free) channel. This variable is incremented
     * as more instanced sources are played.
     */
    private int nextChan = 0;

    /**
     * Channels that have been used and then free'd. If not empty
     * should be used instead of the <code>nextChan</code> variable.
     */
    private ArrayList<Integer> freeChans = new ArrayList<Integer>();

    /**
     * True if the underlying implementation supports the ALC_EXT_EFX
     * extension. This is used for reverb effects in the method
     * setEnvironment().
     */
    private boolean supportEfx = false;
    private int auxSends = 0;

    private int reverbFx = -1;
    private int reverbFxSlot = -1;

    /**
     * Create a new <code>JoalAudioRenderer</code>. Does nothing. Use
     * initialize() to load the OpenAL device.
     */
    public JoalAudioRenderer(){
    }

    /**
     * Loads the default OpenAL audio out device.
     * Prints information about the openal library to the logger.
     * Checks support for EFX.
     * Allocates 16 channel source ids for instanced playback.
     */
    public void initialize() {
        alc = ALFactory.getALC();
        device = alc.alcOpenDevice(null);
        context = alc.alcCreateContext(device, null);

        alc.alcMakeContextCurrent(context);

        al = ALFactory.getAL();
        al.alGetError();

        logger.info("Audio Vendor: "+al.alGetString(AL.AL_VENDOR));
        logger.info("Audio Renderer: "+al.alGetString(AL.AL_RENDERER));
        logger.info("Audio Version: "+al.alGetString(AL.AL_VERSION));

        // Create channel sources
        ib.rewind();
        al.alGenSources(ib.remaining(), ib);
        ib.clear();
        ib.get(channels);
        ib.rewind();

        supportEfx = alc.alcIsExtensionPresent(device, "ALC_EXT_EFX");
        logger.finer("Audio effect extension supported: "+supportEfx);

        // Allocate effect & effect slot for reverb environmental effects
        if (supportEfx){
            ib.position(0).limit(1);
            alc.alcGetIntegerv(device, AL.ALC_EFX_MAJOR_VERSION, 1, ib);
            int major = ib.get(0);
            ib.position(0).limit(1);
            alc.alcGetIntegerv(device, AL.ALC_EFX_MINOR_VERSION, 1, ib);
            int minor = ib.get(0);
            logger.info("Audio effect extension version: "+major+"."+minor);

            ib.position(0).limit(1);
            alc.alcGetIntegerv(device,  AL.ALC_MAX_AUXILIARY_SENDS, 1, ib);
            auxSends = ib.get(0);
            logger.info("Audio max auxilary sends: "+auxSends);

            // create slot
            ib.position(0).limit(1);
            al.alGenAuxiliaryEffectSlots(1, ib);
            reverbFxSlot = ib.get(0);

            // create effect
            ib.position(0).limit(1);
            al.alGenEffects(1, ib);
            reverbFx = ib.get(0);
            al.alEffecti(reverbFx, AL.AL_EFFECT_TYPE, AL.AL_EFFECT_REVERB);
        }
        checkError();
    }

    /**
     * Destroys the 16 channel sources that were allocated.
     * Destroys the context and close the device.
     */
    public void cleanup(){
        // delete channel-based sources
        ib.rewind();
        ib.put(channels);
        ib.flip();
        al.alDeleteSources(ib.remaining(), ib);

        if (supportEfx){
            ib.position(0).limit(1);
            ib.put(0, reverbFx);
            al.alDeleteEffects(1, ib);

            ib.position(0).limit(1);
            ib.put(0, reverbFxSlot);
            al.alDeleteAuxiliaryEffectSlots(1, ib);
        }
        checkError();

        alc.alcMakeContextCurrent(null);
        alc.alcDestroyContext(context);
        alc.alcCloseDevice(device);
        
        alc = null;
        al = null;
        device = null;
        context = null;
    }

    private void checkError(){
        int err = al.alGetError();
        switch (err){
            case AL.AL_INVALID_NAME:
                throw new RuntimeException("OpenAL Error: Invalid Name ("+err+")");
            case AL.AL_INVALID_ENUM:
                throw new RuntimeException("OpenAL Error: Invalid Enum ("+err+")");
            case AL.AL_INVALID_OPERATION:
                throw new RuntimeException("OpenAL Error: Invalid Operation ("+err+")");
            case AL.AL_INVALID_VALUE:
                throw new RuntimeException("OpenAL Error: Invalid Value ("+err+")");
            case AL.AL_OUT_OF_MEMORY:
                throw new RuntimeException("OpenAL Error: Out of Memory ("+err+")");
            default:
                return; // no errors
        }
    }

    /**
     * Sets listener parameters.
     * @param pos The position of the listener
     * @param vel Velocity vector
     * @param dir Direction (look at)
     * @param up Up vector, usually this is 0, 1, 0
     */
    private void setListenerParams(Vector3f pos, Vector3f vel, Vector3f dir, Vector3f up){
        al.alListener3f(AL.AL_POSITION, pos.x, pos.y, pos.z);
        al.alListener3f(AL.AL_VELOCITY, vel.x, vel.y, vel.z);
        fb.rewind();
        fb.put(dir.x).put(dir.y).put(dir.z);
        fb.put(up.x).put(up.y).put(up.z);
        fb.flip();
        al.alListenerfv(AL.AL_ORIENTATION, fb);
    }

    private void updateFilter(Filter f){
        int id = f.getId();
        if (id == -1){
            ib.position(0).limit(1);
            al.alGenFilters(1, ib);
            id = ib.get(0);
            f.setId(id);
        }

        if (f instanceof LowPassFilter){
            LowPassFilter lpf = (LowPassFilter) f;
            al.alFilteri(id, AL.AL_FILTER_TYPE, AL.AL_FILTER_LOWPASS);
            al.alFilterf(id, AL.AL_LOWPASS_GAIN,   lpf.getVolume());
            al.alFilterf(id, AL.AL_LOWPASS_GAINHF, lpf.getHighFreqVolume());
        }else{
            throw new UnsupportedOperationException("Filter type unsupported: "+
                                                    f.getClass().getName());
        }
        checkError();

        f.clearUpdateNeeded();
    }

    private void deleteFilter(Filter f){
        int id = f.getId();
        if (id == -1)
            throw new IllegalStateException("Filter does not exist in AL");

        ib.put(0, id);
        ib.position(0).limit(1);
        al.alDeleteFilters(1, ib);
        checkError();
    }


    private void setSourceParams(int id, AudioNode src, boolean forceNonLoop){
        if (src.isPositional()){
            AudioNode pointSrc = src;
            Vector3f pos = pointSrc.getWorldTranslation();
            Vector3f vel = pointSrc.getVelocity();
            al.alSource3f(id, AL.AL_POSITION, pos.x, pos.y, pos.z);
            al.alSource3f(id, AL.AL_VELOCITY, vel.x, vel.y, vel.z);
            al.alSourcef(id, AL.AL_MAX_DISTANCE, pointSrc.getMaxDistance());
            al.alSourcef(id, AL.AL_REFERENCE_DISTANCE, pointSrc.getRefDistance());
            checkError();

            if (pointSrc.isReverbEnabled()){
                int filter = AL.AL_FILTER_NULL;
                if (pointSrc.getReverbFilter() != null){
                    Filter f = pointSrc.getReverbFilter();
                    if (f.isUpdateNeeded()){
                        updateFilter(f);
                    }
                    filter = f.getId();
                }
                al.alSource3i(id, AL.AL_AUXILIARY_SEND_FILTER, reverbFxSlot, 0, filter);
                checkError();
            }
        }else{
            // play in headspace
            al.alSourcei(id, AL.AL_SOURCE_RELATIVE, AL.AL_TRUE);
            al.alSource3f(id, AL.AL_POSITION, 0,0,0);
            al.alSource3f(id, AL.AL_VELOCITY, 0,0,0);
        }

        if (src.getDryFilter() != null){
            Filter f = src.getDryFilter();
            if (f.isUpdateNeeded()){
                updateFilter(f);
                // NOTE: must re-attach filter for changes to apply.
                al.alSourcei(id, AL.AL_DIRECT_FILTER, f.getId());
                checkError();
            }
        }

        if (forceNonLoop){
            al.alSourcei(id,  AL.AL_LOOPING, AL.AL_FALSE);
        }else{
            al.alSourcei(id,  AL.AL_LOOPING, src.isLooping() ? AL.AL_TRUE : AL.AL_FALSE);
        }

        al.alSourcef(id,  AL.AL_GAIN, src.getVolume());
        al.alSourcef(id,  AL.AL_PITCH, src.getPitch());
        al.alSourcef(id,  AL.AL_SEC_OFFSET, src.getTimeOffset());

        if (src instanceof AudioNode){
            AudioNode das = (AudioNode) src;
            Vector3f dir = das.getDirection();
            al.alSource3f(id, AL.AL_DIRECTION, dir.x, dir.y, dir.z);
            al.alSourcef(id, AL.AL_CONE_INNER_ANGLE, das.getInnerAngle());
            al.alSourcef(id, AL.AL_CONE_OUTER_ANGLE, das.getOuterAngle());
            al.alSourcef(id, AL.AL_CONE_OUTER_GAIN,  0);
        }else{
            al.alSourcef(id, AL.AL_CONE_INNER_ANGLE, 360);
            al.alSourcef(id, AL.AL_CONE_OUTER_ANGLE, 360);
            al.alSourcef(id, AL.AL_CONE_OUTER_GAIN, 1f);
        }
        checkError();
    }
    
    public void setEnvironment(Environment env){
        al.alEffectf(reverbFx, AL.AL_REVERB_DENSITY,             env.getDensity());
        al.alEffectf(reverbFx, AL.AL_REVERB_DIFFUSION,           env.getDiffusion());
        al.alEffectf(reverbFx, AL.AL_REVERB_GAIN,                env.getGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_GAINHF,              env.getGainHf());
        al.alEffectf(reverbFx, AL.AL_REVERB_DECAY_TIME,          env.getDecayTime());
        al.alEffectf(reverbFx, AL.AL_REVERB_DECAY_HFRATIO,       env.getDecayHFRatio());
        al.alEffectf(reverbFx, AL.AL_REVERB_REFLECTIONS_GAIN,    env.getReflectGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_REFLECTIONS_DELAY,   env.getReflectDelay());
        al.alEffectf(reverbFx, AL.AL_REVERB_LATE_REVERB_GAIN,    env.getLateReverbGain());
        al.alEffectf(reverbFx, AL.AL_REVERB_LATE_REVERB_DELAY,   env.getLateReverbDelay());
        al.alEffectf(reverbFx, AL.AL_REVERB_AIR_ABSORPTION_GAINHF, env.getAirAbsorbGainHf());
        al.alEffectf(reverbFx, AL.AL_REVERB_ROOM_ROLLOFF_FACTOR, env.getRoomRolloffFactor());

        // attach effect to slot
        al.alAuxiliaryEffectSloti(reverbFxSlot, AL.AL_EFFECTSLOT_EFFECT, reverbFx);
        checkError();
    }

    private int newChannel(){
        if (freeChans.size() > 0)
            return freeChans.remove(0);
        else if (nextChan < channels.length){
            return nextChan++;
        }else{
            return -1;
        }
    }

    private void freeChannel(int index){
        if (index == nextChan-1)
            nextChan--;
        else
            freeChans.add(index);
    }

    private boolean fillBuffer(AudioStream as, int id){
        int size = 0;
        int result;

        while (size < arrayBuf.length){   
            result = as.readSamples(arrayBuf, size, arrayBuf.length - size);

            if(result > 0){
                size += result;
            }else{
                break;
            }

        }

        if (size == 0)
            return false;
        
        nativeBuf.position(0).limit(nativeBuf.capacity());
        nativeBuf.put(arrayBuf, 0, size);
        nativeBuf.position(0).limit(size);

        al.alBufferData(id, convertFormat(as), nativeBuf, size, as.getSampleRate());
        checkError();

        return true;
    }

    private boolean fillStreamingSource(int sourceId, AudioStream stream){
        if (!stream.isOpen())
            return false;

        boolean active = true;
        ib.position(0).limit(1);
        al.alGetSourcei(sourceId, AL.AL_BUFFERS_PROCESSED, ib);
        int processed = ib.get(0);
        while((processed--) != 0){
            int buffer;

            ib.position(0).limit(1);
            al.alSourceUnqueueBuffers(sourceId, 1, ib);
            buffer = ib.get(0);

            active = fillBuffer(stream, buffer);

            ib.position(0).limit(1);
            ib.put(0, buffer);
            al.alSourceQueueBuffers(sourceId, 1, ib);
        }
        checkError();

        if (!active && stream.isOpen())
            stream.close();

        return active;
    }

    private boolean attachStreamToSource(int sourceId, AudioStream stream){
        boolean active = true;
        for (int id : stream.getIds()){
            active = fillBuffer(stream, id);
            ib.position(0).limit(1);
            ib.put(id).flip();
            al.alSourceQueueBuffers(sourceId, 1, ib);
        }
        checkError();
        return active;
    }

    private boolean attachBufferToSource(int sourceId, AudioBuffer buffer){
        al.alSourcei(sourceId, AL.AL_BUFFER, buffer.getId());
        checkError();
        return true;
    }

    private boolean attachAudioToSource(int sourceId, AudioData data){
        if (data instanceof AudioBuffer){
            return attachBufferToSource(sourceId, (AudioBuffer) data);
        }else if (data instanceof AudioStream){
            return attachStreamToSource(sourceId, (AudioStream) data);
        }
        throw new UnsupportedOperationException();
    }

    private void clearChannel(int index){
        // make room at this channel
        if (chanSrcs[index] != null){
            AudioNode src = chanSrcs[index];

            int sourceId = channels[index];
            al.alSourceStop(sourceId);

            if (src.getAudioData() instanceof AudioStream){
                AudioStream str = (AudioStream) src.getAudioData();
                ib.position(0).limit(STREAMING_BUFFER_COUNT);
                ib.put(str.getIds()).flip();
                al.alSourceUnqueueBuffers(sourceId, STREAMING_BUFFER_COUNT, ib);
            }else if (src.getAudioData() instanceof AudioBuffer){
                al.alSourcei(sourceId, AL.AL_BUFFER, 0);
            }

            if (src.getDryFilter() != null){
                // detach filter
                al.alSourcei(sourceId, AL.AL_DIRECT_FILTER, AL.AL_FILTER_NULL);
            }
            if (src.isPositional()){
                AudioNode pas = (AudioNode) src;
                if (pas.getReverbFilter() != null){
                    al.alSource3i(sourceId, AL.AL_AUXILIARY_SEND_FILTER, 0, 0, AL.AL_FILTER_NULL);
                }
            }

            checkError();

            chanSrcs[index] = null;
        }
    }

    public void update(float tpf){
        for (int i = 0; i < channels.length; i++){
            AudioNode src = chanSrcs[i];
            if (src == null)
                continue;

            int sourceId = channels[i];

            // is the source bound to this channel
            // if false, it's an instanced playback
            boolean boundSource = i == src.getChannel();

            // source's data is streaming
            boolean streaming = src.getAudioData() instanceof AudioStream;

            // only buffered sources can be bound
            assert (boundSource && streaming) || (!streaming);

            ib.position(0).limit(1);
            al.alGetSourcei(sourceId, AL.AL_SOURCE_STATE, ib);
            int state = ib.get(0);
            boolean wantPlaying = src.getStatus() == Status.Playing;
            boolean stopped = state == AL.AL_STOPPED;

            if (streaming && wantPlaying){
                AudioStream stream = (AudioStream) src.getAudioData();
                if (stream.isOpen()){
                    fillStreamingSource(sourceId, stream);
                    if (stopped)
                        al.alSourcePlay(sourceId);
                }else{
                    if (stopped){
                        // became inactive
                        src.setStatus(Status.Stopped);
                        src.setChannel(-1);
                        clearChannel(i);
                        freeChannel(i);
                    }
                }
            }else if (!streaming){
                boolean paused = state == AL.AL_PAUSED;

                // make sure OAL pause state & source state coincide
                assert (src.getStatus() == Status.Paused && paused) || (!paused);

                if (stopped){
                    if (boundSource){
                        src.setStatus(Status.Stopped);
                        src.setChannel(-1);
                    }
                    clearChannel(i);
                    freeChannel(i);
                }
            }
        }

        if (listener != null && listener.isRefreshNeeded()){
            Vector3f pos = listener.getLocation();
            Vector3f vel = listener.getVelocity();
            Vector3f dir = listener.getDirection();
            Vector3f up = listener.getUp();
            setListenerParams(pos, vel, dir, up);
            al.alListenerf(al.AL_GAIN, listener.getGain());
            listener.clearRefreshNeeded();
            checkError();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void playSourceInstance(AudioNode src){
        if (src.getAudioData() instanceof AudioStream)
            throw new UnsupportedOperationException(
                    "Cannot play instances " +
                    "of audio streams. Use playSource() instead.");

        if (src.getAudioData().isUpdateNeeded()){
            updateAudioData(src.getAudioData());
        }

        // create a new index for an audio-channel
        int index = newChannel();
        int sourceId = channels[index];

        clearChannel(index);

        // set parameters, like position and max distance
        setSourceParams(sourceId, src, true);
        attachAudioToSource(sourceId, src.getAudioData());
        chanSrcs[index] = src;

        // play the channel
        al.alSourcePlay(sourceId);

        System.out.println("Playing on "+index);
    }

    public void playSource(AudioNode src) {
//        assert src.getStatus() == Status.Stopped || src.getChannel() == -1;

        if (src.getStatus() == Status.Playing){
            return;
        }else if (src.getStatus() == Status.Stopped){
            // allocate channel to this source
            int index = newChannel();
            clearChannel(index);
            src.setChannel(index);

            AudioData data = src.getAudioData();
            if (data.isUpdateNeeded())
                updateAudioData(data);

            chanSrcs[index] = src;
            setSourceParams(channels[index], src, false);
            attachAudioToSource(channels[index], data);
        }

        al.alSourcePlay(channels[src.getChannel()]);
        src.setStatus(Status.Playing);
    }

    public void pauseSource(AudioNode src) {
        if (src.getStatus() == Status.Playing){
            assert src.getChannel() != -1;

            al.alSourcePause(channels[src.getChannel()]);
            src.setStatus(Status.Paused);
        }
    }

    public void stopSource(AudioNode src) {
        if (src.getStatus() != Status.Stopped){
            int chan = src.getChannel();
            assert chan != -1; // if it's not stopped, must have id

            src.setStatus(Status.Stopped);
            src.setChannel(-1);
            clearChannel(chan);
            freeChannel(chan);
        }
    }

    private int convertFormat(AudioData ad){
        switch (ad.getBitsPerSample()){
            case 8:
                if (ad.getChannels() == 1)
                    return AL.AL_FORMAT_MONO8;
                else if (ad.getChannels() == 2)
                    return AL.AL_FORMAT_STEREO8;

                break;
            case 16:
                if (ad.getChannels() == 1)
                    return AL.AL_FORMAT_MONO16;
                else
                    return AL.AL_FORMAT_STEREO16;
        }
        throw new UnsupportedOperationException("Unsupported channels/bits combination: "+
                                                "bits="+ad.getBitsPerSample()+", channels="+ad.getChannels());
    }

    public void updateAudioBuffer(AudioBuffer ab){
        int id = ab.getId();
        if (ab.getId() == -1){
            ib.position(0).limit(1);
            al.alGenBuffers(1, ib);
            id = ib.get(0);
            ab.setId(id);
        }

        al.alBufferData(id, convertFormat(ab), ab.getData(), ab.getData().remaining(), ab.getSampleRate());
        checkError();

        ab.clearUpdateNeeded();
    }

    public void updateAudioStream(AudioStream as){
        if (as.getIds() != null){
            deleteAudioData(as);
        }

        int[] ids = new int[STREAMING_BUFFER_COUNT];
        ib.position(0).limit(STREAMING_BUFFER_COUNT);
        al.alGenBuffers(STREAMING_BUFFER_COUNT, ib);
        ib.position(0).limit(STREAMING_BUFFER_COUNT);
        ib.get(ids);
        checkError();

        as.setIds(ids);
        as.clearUpdateNeeded();
    }

    public void updateAudioData(AudioData ad){
        if (ad instanceof AudioBuffer){
            updateAudioBuffer((AudioBuffer) ad);
        }else if (ad instanceof AudioStream){
            updateAudioStream((AudioStream) ad);
        }
    }

    public void deleteSource(AudioNode src){
        AudioData data = src.getAudioData();
        
    }

    public void deleteAudioData(AudioData ad){
        if (ad instanceof AudioBuffer){
            AudioBuffer ab = (AudioBuffer) ad;
            int id = ab.getId();
            if (id != -1){
                ib.put(0,id);
                ib.position(0).limit(1);
                al.alDeleteBuffers(1, ib);
                ab.resetObject();
                checkError();
            }
        }else if (ad instanceof AudioStream){
            AudioStream as = (AudioStream) ad;
            int[] ids = as.getIds();
            if (ids != null){
                ib.clear();
                ib.put(ids).flip();
                al.alDeleteBuffers(ids.length, ib);
                as.resetObject();
                checkError();
            }
        }
    }

}

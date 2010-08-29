package com.jme3.audio.lwjgl;

import org.lwjgl.openal.AL10;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioNode.Status;
import com.jme3.audio.AudioStream;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.LowPassFilter;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCdevice;
import org.lwjgl.openal.EFX10;
import org.lwjgl.openal.OpenALException;

import static org.lwjgl.openal.AL10.*;

public class LwjglAudioRenderer implements AudioRenderer {

    private static final Logger logger = Logger.getLogger(LwjglAudioRenderer.class.getName());

    private static final int BUFFER_SIZE = 8192;
    private static final int STREAMING_BUFFER_COUNT = 5;

    private final IntBuffer ib = BufferUtils.createIntBuffer(16);
    private final FloatBuffer fb = BufferUtils.createVector3Buffer(2);
    private final ByteBuffer nativeBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final byte[] arrayBuf = new byte[BUFFER_SIZE];

    private int[] channels = new int[16];
    private AudioNode[] chanSrcs = new AudioNode[16];
    private int nextChan = 0;
    private ArrayList<Integer> freeChans = new ArrayList<Integer>();

    private Listener listener;
    private boolean audioDisabled = false;

    private boolean supportEfx = false;
    private int auxSends = 0;
    private int reverbFx = -1;
    private int reverbFxSlot = -1;

    public LwjglAudioRenderer(){
        nativeBuf.order(ByteOrder.nativeOrder());
    }

    public void initialize(){
        try{
            AL.create();
        }catch (OpenALException ex){
            logger.log(Level.SEVERE, "Failed to load audio library", ex);
            audioDisabled = true;
            return;
        }catch (LWJGLException ex){
            logger.log(Level.SEVERE, "Failed to load audio library", ex);
            audioDisabled = true;
            return;
        }

        logger.finer("Audio Vendor: "+alGetString(AL_VENDOR));
        logger.finer("Audio Renderer: "+alGetString(AL_RENDERER));
        logger.finer("Audio Version: "+alGetString(AL_VERSION));

        // Create channel sources
        ib.clear();
        ib.limit(channels.length);
        alGenSources(ib);
        ib.clear();
        ib.get(channels);
        ib.clear();

        ALCdevice device = AL.getDevice();
        supportEfx = ALC10.alcIsExtensionPresent(device, "ALC_EXT_EFX");
        logger.finer("Audio EFX support: " + supportEfx);

        if (supportEfx){
            ib.position(0).limit(1);
            ALC10.alcGetInteger(device, EFX10.ALC_EFX_MAJOR_VERSION, ib);
            int major = ib.get(0);
            ib.position(0).limit(1);
            ALC10.alcGetInteger(device, EFX10.ALC_EFX_MINOR_VERSION, ib);
            int minor = ib.get(0);
            logger.info("Audio effect extension version: "+major+"."+minor);

            ALC10.alcGetInteger(device, EFX10.ALC_MAX_AUXILIARY_SENDS, ib);
            auxSends = ib.get(0);
            logger.info("Audio max auxilary sends: "+auxSends);

            // create slot
            ib.position(0).limit(1);
            EFX10.alGenAuxiliaryEffectSlots(ib);
            reverbFxSlot = ib.get(0);

            // create effect
            ib.position(0).limit(1);
            EFX10.alGenEffects(ib);
            reverbFx = ib.get(0);
            EFX10.alEffecti(reverbFx, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);

            // attach reverb effect to effect slot
//            EFX10.alAuxiliaryEffectSloti(reverbFxSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbFx);
        }
    }

    public void cleanup(){
        if (audioDisabled)
            return;

        // delete channel-based sources
        ib.clear();
        ib.put(channels);
        ib.flip();
        alDeleteSources(ib);

        if (supportEfx){
            ib.position(0).limit(1);
            ib.put(0, reverbFx);
            EFX10.alDeleteEffects(ib);

            ib.position(0).limit(1);
            ib.put(0, reverbFxSlot);
            EFX10.alDeleteAuxiliaryEffectSlots(ib);
        }

        // XXX: Delete other buffers/sources
        AL.destroy();
    }

    private void setListenerParams(Vector3f pos, Vector3f vel, Vector3f dir, Vector3f up){
        alListener3f(AL_POSITION, pos.x, pos.y, pos.z);
        alListener3f(AL_VELOCITY, vel.x, vel.y, vel.z);
        fb.rewind();
        fb.put(dir.x).put(dir.y).put(dir.z);
        fb.put(up.x).put(up.y).put(up.z);
        fb.flip();
        alListener(AL_ORIENTATION, fb);
    }

    private void updateFilter(Filter f){
        int id = f.getId();
        if (id == -1){
            ib.position(0).limit(1);
            EFX10.alGenFilters(ib);
            id = ib.get(0);
            f.setId(id);
        }

        if (f instanceof LowPassFilter){
            LowPassFilter lpf = (LowPassFilter) f;
            EFX10.alFilteri(id, EFX10.AL_FILTER_TYPE,    EFX10.AL_FILTER_LOWPASS);
            EFX10.alFilterf(id, EFX10.AL_LOWPASS_GAIN,   lpf.getVolume());
            EFX10.alFilterf(id, EFX10.AL_LOWPASS_GAINHF, lpf.getHighFreqVolume());
        }else{
            throw new UnsupportedOperationException("Filter type unsupported: "+
                                                    f.getClass().getName());
        }

        f.clearUpdateNeeded();
    }

    private void setSourceParams(int id, AudioNode src, boolean forceNonLoop){
        if (src.isPositional()){
            AudioNode pointSrc = src;
            Vector3f pos = pointSrc.getWorldTranslation();
            Vector3f vel = pointSrc.getVelocity();
            alSource3f(id, AL_POSITION, pos.x, pos.y, pos.z);
            alSource3f(id, AL_VELOCITY, vel.x, vel.y, vel.z);
            alSourcef(id, AL_MAX_DISTANCE, pointSrc.getMaxDistance());
            alSourcef(id, AL_REFERENCE_DISTANCE, pointSrc.getRefDistance());

            if (pointSrc.isReverbEnabled()){
                int filter = EFX10.AL_FILTER_NULL;
                if (pointSrc.getReverbFilter() != null){
                    Filter f = pointSrc.getReverbFilter();
                    if (f.isUpdateNeeded()){
                        updateFilter(f);
                    }
                    filter = f.getId();
                }
                AL11.alSource3i(id, EFX10.AL_AUXILIARY_SEND_FILTER, reverbFxSlot, 0, filter);
            }
        }else{
            // play in headspace
            alSourcei(id, AL_SOURCE_RELATIVE, AL_TRUE);
            alSource3f(id, AL_POSITION, 0,0,0);
            alSource3f(id, AL_VELOCITY, 0,0,0);
        }

        if (src.getDryFilter() != null){
            Filter f = src.getDryFilter();
            if (f.isUpdateNeeded()){
                updateFilter(f);
                
                // NOTE: must re-attach filter for changes to apply.
                alSourcei(id, EFX10.AL_DIRECT_FILTER, f.getId());
            }
        }

        if (forceNonLoop){
            alSourcei(id,  AL_LOOPING, AL_FALSE);
        }else{
            alSourcei(id,  AL_LOOPING, src.isLooping() ? AL_TRUE : AL_FALSE);
        }
        alSourcef(id,  AL_GAIN, src.getVolume());
        alSourcef(id,  AL_PITCH, src.getPitch());
        alSourcef(id,  AL11.AL_SEC_OFFSET, src.getTimeOffset());

        if (src.isDirectional()){
            AudioNode das = src;
            Vector3f dir = das.getDirection();
            alSource3f(id, AL_DIRECTION, dir.x, dir.y, dir.z);
            alSourcef(id, AL_CONE_INNER_ANGLE, das.getInnerAngle());
            alSourcef(id, AL_CONE_OUTER_ANGLE, das.getOuterAngle());
            alSourcef(id, AL_CONE_OUTER_GAIN,  0);
        }else{
            alSourcef(id, AL_CONE_INNER_ANGLE, 360);
            alSourcef(id, AL_CONE_OUTER_ANGLE, 360);
            alSourcef(id, AL_CONE_OUTER_GAIN, 1f);
        }
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
    
    public void setEnvironment(Environment env){
        if (audioDisabled)
            return;

        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_DENSITY,             env.getDensity());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_DIFFUSION,           env.getDiffusion());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_GAIN,                env.getGain());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_GAINHF,              env.getGainHf());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_DECAY_TIME,          env.getDecayTime());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_DECAY_HFRATIO,       env.getDecayHFRatio());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_REFLECTIONS_GAIN,    env.getReflectGain());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_REFLECTIONS_DELAY,   env.getReflectDelay());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_LATE_REVERB_GAIN,    env.getLateReverbGain());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_LATE_REVERB_DELAY,   env.getLateReverbDelay());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_AIR_ABSORPTION_GAINHF, env.getAirAbsorbGainHf());
        EFX10.alEffectf(reverbFx, EFX10.AL_REVERB_ROOM_ROLLOFF_FACTOR, env.getRoomRolloffFactor());

        // attach effect to slot
        EFX10.alAuxiliaryEffectSloti(reverbFxSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbFx);
//        logger.warning("Reverb not supported by LWJGL renderer");
    }

    private boolean fillBuffer(AudioStream stream, int id){
        int size = 0;
        int result;

        while (size < arrayBuf.length){
            result = stream.readSamples(arrayBuf, size, arrayBuf.length - size);

            if(result > 0){
                size += result;
            }else{
                break;
            }
        }

        if(size == 0)
            return false;

        nativeBuf.clear();
        nativeBuf.put(arrayBuf, 0, size);
        nativeBuf.flip();

        alBufferData(id, convertFormat(stream), nativeBuf, stream.getSampleRate());

        return true;
    }

    private boolean fillStreamingSource(int sourceId, AudioStream stream){
        if (!stream.isOpen())
            return false;

        boolean active = true;
        int processed = alGetSourcei(sourceId, AL_BUFFERS_PROCESSED);

        while((processed--) != 0){
            int buffer;

            ib.position(0).limit(1);
            alSourceUnqueueBuffers(sourceId, ib);
            buffer = ib.get(0);

            active = fillBuffer(stream, buffer);

            ib.position(0).limit(1);
            ib.put(0, buffer);
            alSourceQueueBuffers(sourceId, ib);
        }

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
            alSourceQueueBuffers(sourceId, ib);
        }
        return active;
    }

    private boolean attachBufferToSource(int sourceId, AudioBuffer buffer){
        alSourcei(sourceId, AL_BUFFER, buffer.getId());
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
            alSourceStop(sourceId);

            if (src.getAudioData() instanceof AudioStream){
                AudioStream str = (AudioStream) src.getAudioData();
                ib.position(0).limit(STREAMING_BUFFER_COUNT);
                ib.put(str.getIds()).flip();
                alSourceUnqueueBuffers(sourceId, ib);
            }else if (src.getAudioData() instanceof AudioBuffer){
                alSourcei(sourceId, AL_BUFFER, 0);
            }

            if (src.getDryFilter() != null){
                // detach filter
                alSourcei(sourceId, EFX10.AL_DIRECT_FILTER, EFX10.AL_FILTER_NULL);
            }
            if (src.isPositional()){
                AudioNode pas = (AudioNode) src;
                if (pas.getReverbFilter() != null){
                    AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, 0, 0, EFX10.AL_FILTER_NULL);
                }
            }

            chanSrcs[index] = null;
        }
    }

    public void update(float tpf){
        if (audioDisabled)
            return;

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

            int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
            boolean wantPlaying = src.getStatus() == Status.Playing;
            boolean stopped = state == AL_STOPPED || state == AL_PAUSED;

            if (streaming && wantPlaying){
                AudioStream stream = (AudioStream) src.getAudioData();
                if (stream.isOpen()){
                    fillStreamingSource(sourceId, stream);
                    if (stopped)
                        alSourcePlay(sourceId);
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
                boolean paused = state == AL_PAUSED;

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

        // update listener
        if (listener != null && listener.isRefreshNeeded()){
            Vector3f pos = listener.getLocation();
            Vector3f vel = listener.getVelocity();
            Vector3f dir = listener.getDirection();
            Vector3f up = listener.getUp();
            setListenerParams(pos, vel, dir, up);
            alListenerf(AL_GAIN, listener.getGain());
            listener.clearRefreshNeeded();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void playSourceInstance(AudioNode src){
        if (audioDisabled)
            return;

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
        alSourcePlay(sourceId);

        System.out.println("Playing on "+index);
    }

    
    public void playSource(AudioNode src) {
        if (audioDisabled)
            return;

        assert src.getStatus() == Status.Stopped || src.getChannel() == -1;

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

        alSourcePlay(channels[src.getChannel()]);
        src.setStatus(Status.Playing);
    }

    
    public void pauseSource(AudioNode src) {
        if (audioDisabled)
            return;

        if (src.getStatus() == Status.Playing){
            assert src.getChannel() != -1;

            alSourcePause(channels[src.getChannel()]);
            src.setStatus(Status.Paused);
        }
    }

    
    public void stopSource(AudioNode src) {
        if (audioDisabled)
            return;

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
                    return AL_FORMAT_MONO8;
                else if (ad.getChannels() == 2)
                    return AL_FORMAT_STEREO8;

                break;
            case 16:
                if (ad.getChannels() == 1)
                    return AL_FORMAT_MONO16;
                else
                    return AL_FORMAT_STEREO16;
        }
        throw new UnsupportedOperationException("Unsupported channels/bits combination: "+
                                                "bits="+ad.getBitsPerSample()+", channels="+ad.getChannels());
    }

    public void updateAudioBuffer(AudioBuffer ab){
        int id = ab.getId();
        if (ab.getId() == -1){
            ib.position(0).limit(1);
            alGenBuffers(ib);
            id = ib.get(0);
            ab.setId(id);
        }

        ab.getData().clear();
        alBufferData(id, convertFormat(ab), ab.getData(), ab.getSampleRate());
        ab.clearUpdateNeeded();
    }

    public void updateAudioStream(AudioStream as){
        if (as.getIds() != null){
            deleteAudioData(as);
        }

        int[] ids = new int[STREAMING_BUFFER_COUNT];
        ib.position(0).limit(STREAMING_BUFFER_COUNT);
        alGenBuffers(ib);
        ib.position(0).limit(STREAMING_BUFFER_COUNT);
        ib.get(ids);
        
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

    public void deleteAudioData(AudioData ad){
        if (audioDisabled)
            return;
        
        if (ad instanceof AudioBuffer){
            AudioBuffer ab = (AudioBuffer) ad;
            int id = ab.getId();
            if (id != -1){
                ib.put(0,id);
                ib.position(0).limit(1);
                alDeleteBuffers(ib);
                ab.resetObject();
            }
        }else if (ad instanceof AudioStream){
            AudioStream as = (AudioStream) ad;
            int[] ids = as.getIds();
            if (ids != null){
                ib.clear();
                ib.put(ids).flip();
                alDeleteBuffers(ib);
                as.resetObject();
            }
        }
    }

}

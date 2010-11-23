/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * OpenGL ES 2 Android Renderer
 *
 * created: Sun Nov  7 22:54:15 EST 2010
 * based on LwjglRenderer.java
 *
 */

package com.jme3.renderer.android;

import com.jme3.light.LightList;
import com.jme3.material.RenderState;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;


import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.Mesh.Mode;

import com.jme3.shader.Shader;
import com.jme3.shader.Shader.ShaderSource;
import com.jme3.shader.Uniform;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.Attribute;

import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapAxis;
import com.jme3.texture.Image;

import com.jme3.util.BufferUtils;
import com.jme3.util.ListMap;
import com.jme3.util.IntMap;
import com.jme3.util.IntMap.Entry;

import com.jme3.renderer.Caps;
import com.jme3.renderer.GLObjectManager;
import com.jme3.renderer.Statistics;
import com.jme3.renderer.RenderContext;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.IDList;



import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;

import java.util.Collection;
import java.util.List;
import java.util.EnumSet;

import java.util.logging.Level;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;



public class OGLESShaderRenderer implements com.jme3.renderer.Renderer {

	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OGLESShaderRenderer.class.getName());


	/* The reference to the OpenGL context passed from the caller of the constructor.
	 * This renderer does not use this reference.
	 */

	private GL10 gl = null;


	/*
	 * the constructor takes gl as a parameter, but the renderer does not use this reference.
	 * android.opengl.GLES20 static method are used instead.
	 */

	public OGLESShaderRenderer(GL10 gl){
		this.gl = gl;
	}


	private EnumSet<Caps> caps = EnumSet.noneOf(Caps.class);

	public Collection<Caps> getCaps() {
		return caps;
	}


	private Statistics statistics = new Statistics();

	public Statistics getStatistics() {
		return statistics;
	}

	private boolean verboseLoggingEnabled = false;


	public void clearBuffers(boolean color, boolean depth, boolean stencil) {
		logger.info("clearBuffers(color=" + color + ", depth=" + depth + ", stencil=" + stencil + ")");

		int bits = 0;

		if (color)
			bits = GLES20.GL_COLOR_BUFFER_BIT;

		if (depth)
			bits |= GLES20.GL_DEPTH_BUFFER_BIT;

		if (stencil)
			bits |= GLES20.GL_STENCIL_BUFFER_BIT;

		if (bits != 0)
			GLES20.glClear(bits);
	}

	public void setBackgroundColor(ColorRGBA color) {
		if (verboseLoggingEnabled)
			logger.info("GLES20.glClearColor(" + color + ")");
		GLES20.glClearColor(color.r, color.g, color.b, color.a);
	}


	private RenderContext context = new RenderContext();
	private GLObjectManager objManager = new GLObjectManager();

	/**
	* Applies the given renderstate, making the neccessary
	* GL calls so that the state is applied.
	*/

	public void applyRenderState(RenderState state) {

	/*
		no GL.LINE, GL_FILL

		if (state.isWireframe() && !context.wireframe){
			gl.glPolygonMode(gl.GL_FRONT_AND_BACK, GLES20.GL_LINE);
			context.wireframe = true;
		} else if (!state.isWireframe() && context.wireframe) {
			gl.glPolygonMode(gl.GL_FRONT_AND_BACK, GLES20.GL_FILL);
			context.wireframe = false;
		}
	*/

		if (verboseLoggingEnabled)
			logger.info("applyRenderState(" + state + ")");

		if (state.isDepthTest() && !context.depthTestEnabled) {
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LEQUAL);
			context.depthTestEnabled = true;
		} else if (!state.isDepthTest() && context.depthTestEnabled) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			context.depthTestEnabled = false;
		}

		if (state.isAlphaTest() && !context.alphaTestEnabled) {
	//		GLES20.glEnable(GLES20.GL_ALPHA_TEST);
	//		GLES20.glAlphaFunc(GLES20.GL_GREATER, state.getAlphaFallOff());
			context.alphaTestEnabled = true;
		} else if (!state.isAlphaTest() && context.alphaTestEnabled) {
	//		GLES20.glDisable(GLES20.GL_ALPHA_TEST);
			context.alphaTestEnabled = false;
		}

		if (state.isDepthWrite() && !context.depthWriteEnabled) {
			GLES20.glDepthMask(true);
			context.depthWriteEnabled = true;
		} else if (!state.isDepthWrite() && context.depthWriteEnabled) {
			GLES20.glDepthMask(false);
			context.depthWriteEnabled = false;
		}

		if (state.isColorWrite() && !context.colorWriteEnabled) {
			GLES20.glColorMask(true, true, true, true);
			context.colorWriteEnabled = true;
		} else if (!state.isColorWrite() && context.colorWriteEnabled) {
			GLES20.glColorMask(false, false, false, false);
			context.colorWriteEnabled = false;
		}

		if (state.isPolyOffset()) {
			if (!context.polyOffsetEnabled) {
				GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
				GLES20.glPolygonOffset(
					state.getPolyOffsetFactor(),
					state.getPolyOffsetUnits());
					context.polyOffsetEnabled = true;
					context.polyOffsetFactor = state.getPolyOffsetFactor();
					context.polyOffsetUnits = state.getPolyOffsetUnits();
			} else {
				if (
					state.getPolyOffsetFactor() != context.polyOffsetFactor ||
					state.getPolyOffsetUnits() != context.polyOffsetUnits
				) {
					GLES20.glPolygonOffset(
						state.getPolyOffsetFactor(),
						state.getPolyOffsetUnits()
					);
					context.polyOffsetFactor = state.getPolyOffsetFactor();
					context.polyOffsetUnits = state.getPolyOffsetUnits();
				}
			}
		} else {
			if (context.polyOffsetEnabled) {
				GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
				context.polyOffsetEnabled = false;
				context.polyOffsetFactor = 0;
				context.polyOffsetUnits = 0;
			}
		}

		if (state.getFaceCullMode() != context.cullMode) {
			if (state.getFaceCullMode() == RenderState.FaceCullMode.Off)
				GLES20.glDisable(GLES20.GL_CULL_FACE);
			else
				GLES20.glEnable(GLES20.GL_CULL_FACE);

			switch (state.getFaceCullMode()) {
				case Off:
					break;
				case Back:
					GLES20.glCullFace(GLES20.GL_BACK);
 					break;
				case Front:
					GLES20.glCullFace(GLES20.GL_FRONT);
					break;
				case FrontAndBack:
					GLES20.glCullFace(GLES20.GL_FRONT_AND_BACK);
					break;
				default:
					throw new UnsupportedOperationException(
						"Unrecognized face cull mode: " + state.getFaceCullMode()
					);
			}

			context.cullMode = state.getFaceCullMode();
		}

		if (state.getBlendMode() != context.blendMode) {
			if (state.getBlendMode() == RenderState.BlendMode.Off)
				GLES20.glDisable(GLES20.GL_BLEND);
			else
				GLES20.glEnable(GLES20.GL_BLEND);

			switch (state.getBlendMode()) {
				case Off:
					break;
				case Additive:
					GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
					break;
				case AlphaAdditive:
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
					break;
				case Color:
					GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_COLOR);
					break;
				case Alpha:
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case PremultAlpha:
					GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case Modulate:
					GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
					break;
				case ModulateX2:
					GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_SRC_COLOR);
					break;
				default:
					throw new UnsupportedOperationException(
						"Unrecognized blend mode: " + state.getBlendMode()
					);
			}

			context.blendMode = state.getBlendMode();
		}
	}

	/**
	* Set the range of the depth values for objects. 
	* @param start
	* @param end
	*/
	public void setDepthRange(float start, float end) {
		GLES20.glDepthRangef(start, end);
	}


	/**
	* Called when a new frame has been rendered.
	*/
	public void onFrame() {
		objManager.deleteUnused(this);
	}

	/**
	* @param transform The world transform to use. This changes
	* the world matrix given in the shader.
	*/
	public void setWorldMatrix(Matrix4f worldMatrix) {
		logger.warning("setWorldMatrix: not implemented.");
	}


	public void setViewProjectionMatrices(Matrix4f viewMatrix, Matrix4f projMatrix) {
		logger.warning("setViewProjectionMatrices: not implemented.");
	}

	private int vpX, vpY, vpW, vpH;

	public void setViewPort(int x, int y, int width, int height) {
		if (verboseLoggingEnabled)
			logger.info("setViewPort(" + x + ", " + y + ", " + width + ", " + height + ")");
		GLES20.glViewport(x, y, width, height);
		vpX = x;
		vpY = y;
		vpW = width;
		vpH = height;
	}


	public void setClipRect(int x, int y, int width, int height) {
		if (!context.clipRectEnabled) {
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			context.clipRectEnabled = true;
		}
		GLES20.glScissor(x, y, width, height);
	}


	public void clearClipRect() {
		if (context.clipRectEnabled) {
			GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
			context.clipRectEnabled = false;
		}
	}

	public void setLighting(LightList list) {
		logger.warning("setLighting: not implemented.");
	}


	private IntBuffer intBuf1 = BufferUtils.createIntBuffer(1);
	private Shader boundShader;
	private boolean VALIDATE_SHADER = false;
 

	/**
	* @param shader Sets the shader to use for rendering, uploading it
	* if neccessary.
	*/
	public void setShader(Shader shader) {

		if (verboseLoggingEnabled)
			logger.info("setShader(" + shader + ")");

		if (shader == null) {
			if (context.boundShaderProgram > 0) {
				logger.info("GLES20.glUseProgram(0) ...");
				GLES20.glUseProgram(0);
				logger.info("GLES20.glUseProgram(0) done.");
				statistics.onShaderUse(null, true);
				context.boundShaderProgram = 0;
				boundShader = null;
			}
		} else {
			if (shader.isUpdateNeeded())
				updateShaderData(shader);

			// NOTE: might want to check if any of the 
			// sources need an update?

			if (!shader.isUsable())
				return;

			assert shader.getId() > 0;

			updateShaderUniforms(shader);

			if (context.boundShaderProgram != shader.getId()) {
				if (VALIDATE_SHADER) {
					// check if shader can be used
					// with current state

					logger.info("GLES20.glValidateProgram(" + shader.getId() + ") ...");
					GLES20.glValidateProgram(shader.getId());
					logger.info("GLES20.glValidateProgram(" + shader.getId() + ") ... done");
					logger.info("GLES20.glGetProgramiv() ...");
					GLES20.glGetProgramiv(shader.getId(), GLES20.GL_VALIDATE_STATUS, intBuf1);
					logger.info("GLES20.glGetProgramiv() ... done.");

					boolean validateOK = intBuf1.get(0) == GLES20.GL_TRUE;

					if (validateOK) {
						logger.info("shader validate success");
					} else {
						logger.warning("shader validate failure");
					}
				}

				logger.info("GLES20.glUseProgram(0) ...");
				GLES20.glUseProgram(shader.getId());
				logger.info("GLES20.glUseProgram(0) ... done.");
				statistics.onShaderUse(shader, true);
				context.boundShaderProgram = shader.getId();
				boundShader = shader;
			} else {
				statistics.onShaderUse(shader, false);
			}
		}
	}

	public void updateShaderData(Shader shader) {

		int id = shader.getId();

		boolean needRegister = false;

		if (id == -1) {
			// create program

			logger.info("GLES20.glCreateProgram() ...");
			id = GLES20.glCreateProgram();
			logger.info("GLES20.glCreateProgram() ... done.");

			if (id <= 0)
				throw new RendererException("Invalid ID received when trying to create shader program.");

			shader.setId(id);

			needRegister = true;
		}

		for (ShaderSource source : shader.getSources()) {
			if (source.isUpdateNeeded()) {
				updateShaderSourceData(source, shader.getLanguage());
				// shader has been compiled here
			}

			if (!source.isUsable()){
				// it's useless.. just forget about everything..
				shader.setUsable(false);
				shader.clearUpdateNeeded();
				return;
			}

			logger.info("GLES20.glAttachShader(" + id + ", " + source.getId() + ") ...");
			GLES20.glAttachShader(id, source.getId());
			logger.info("GLES20.glAttachShader(" + id + ", " + source.getId() + ") ... done");
		}

		// link shaders to program
		logger.info("GLES20.glLinkProgram(" + id + ") ...");
		GLES20.glLinkProgram(id);
		logger.info("GLES20.glLinkProgram(" + id + ") ... done.");

		logger.info("GLES20.glGetProgramiv(" + id + ") ...");
		GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, intBuf1);
		logger.info("GLES20.glGetProgramiv(" + id + ") ... done.");
		boolean linkOK = intBuf1.get(0) == GLES20.GL_TRUE;
		String infoLog = null;
        
		if (VALIDATE_SHADER || !linkOK) {
			logger.info("GLES20.glGetProgramiv(" + id + ") ...");
			GLES20.glGetProgramiv(id, GLES20.GL_INFO_LOG_LENGTH, intBuf1);
			logger.info("GLES20.glGetProgramiv(" + id + ") ... done.");
			int length = intBuf1.get(0);
			if (length > 3) {
				// get infos
/*
				Lwgjl implementation

				ByteBuffer logBuf = BufferUtils.createByteBuffer(length);
				glGetProgramInfoLog(id, null, logBuf);

				// convert to string, etc
				byte[] logBytes = new byte[length];
				logBuf.get(logBytes, 0, length);
				infoLog = new String(logBytes);
*/
				logger.info("GLES20.glGetProgramInfoLog(" + id + ") ...");
				infoLog = GLES20.glGetProgramInfoLog(id);
				logger.info("GLES20.glGetProgramInfoLog(" + id + ") ... done.");
			}
		}

		if (linkOK) {
			if (infoLog != null) {
				logger.log(Level.INFO, "shader link success. \n{0}", infoLog);
			} else {
				logger.info("shader link success");
			}
		} else {
			if (infoLog != null) {
				logger.log(Level.WARNING, "shader link failure. \n{0}", infoLog);
			} else {
				logger.warning("shader link failure");
			}
		}

		shader.clearUpdateNeeded();

		if (!linkOK){
			// failure.. forget about everything
			shader.resetSources();
			shader.setUsable(false);
			deleteShader(shader);
		} else {
			shader.setUsable(true);
			if (needRegister) {
				objManager.registerForCleanup(shader);
				statistics.onNewShader();
			} else {
				// OpenGL spec: uniform locations may change after re-link
				resetUniformLocations(shader);
			}
		}
	}

	protected void updateShaderUniforms(Shader shader) {
		ListMap<String, Uniform> uniforms = shader.getUniformMap();
		//        for (Uniform uniform : shader.getUniforms()){
		for (int i = 0; i < uniforms.size(); i++) {
			Uniform uniform = uniforms.getValue(i);
			if (uniform.isUpdateNeeded())
				updateUniform(shader, uniform);
		}
	}

	protected void updateShaderSourceData(ShaderSource source, String language) {
		int id = source.getId();

		if (id == -1) {
			// create id
			logger.info("GLES20.glCreateShader() ...");
			id = GLES20.glCreateShader(convertShaderType(source.getType()));
			logger.info("GLES20.glCreateShader() ... done.");

			if (id <= 0)
				throw new RendererException("Invalid ID received when trying to create shader.");

			source.setId(id);
		}

		// upload shader source
		// merge the deinfos and source code


		byte[] precisionDeclaration = "precision mediump float;\n".getBytes();

		byte[] versionData = new byte[]{};//"#version 140\n".getBytes();
		//        versionData = "#deinfo INSTANCING 1\n".getBytes();
		byte[] deinfosCodeData = source.getDefines().getBytes();
		byte[] sourceCodeData = source.getSource().getBytes();

		ByteBuffer codeBuf = BufferUtils.createByteBuffer(
			precisionDeclaration.length +
			versionData.length +
			deinfosCodeData.length +
			sourceCodeData.length
		);

		codeBuf.put(precisionDeclaration);
		codeBuf.put(versionData);
		codeBuf.put(deinfosCodeData);
		codeBuf.put(sourceCodeData);
		codeBuf.flip();

		logger.info("shader code buffer length: [" + codeBuf.limit() + "]");

		byte[] codeBufBytes = new byte[codeBuf.limit()];
		codeBuf.get(codeBufBytes, 0, codeBuf.limit());
		String codeString = new String(codeBufBytes);
 
		logger.info("GLES20.glShaderSource() ...");
		GLES20.glShaderSource(id, codeString);
		logger.info("GLES20.glShaderSource() ... done.");
		logger.info("GLES20.glCompileShader(" + id + ") ...");
		GLES20.glCompileShader(id);
		logger.info("GLES20.glCompileShader(" + id + ") ... done.");

		logger.info("GLES20.glShaderiv() ...");
		GLES20.glGetShaderiv(id, GLES20.GL_COMPILE_STATUS, intBuf1);
		logger.info("GLES20.glShaderiv() ... done.");

		boolean compiledOK = intBuf1.get(0) == GLES20.GL_TRUE;

		String infoLog = null;

		if (VALIDATE_SHADER || !compiledOK) {
			// even if compile succeeded, check
			// log for warnings
			logger.info("GLES20.glShaderiv() ...");
			GLES20.glGetShaderiv(id, GLES20.GL_INFO_LOG_LENGTH, intBuf1);
			logger.info("GLES20.glShaderiv() ... done.");

			int length = intBuf1.get(0);

			if (length > 3)
				infoLog = GLES20.glGetShaderInfoLog(id);
		}

		if (compiledOK){
			if (infoLog != null) {
				logger.log(
					Level.INFO,
					"{0} compile success\n{1}",
					new Object[]{source.getName(), infoLog}
				);
			} else {
				logger.log(Level.FINE, "{0} compile success", source.getName());
			}
		} else {
			if (infoLog != null) {
				logger.warning(
					"compile error [" + source.getName() + "]: [" + infoLog + "]."
				);
			} else {
				logger.warning("compile error [" + source.getName() + "].");
			}

			logger.warning(
				"deinfos: [" + source.getDefines() + "]\nsource: [" + source.getSource() + "]"
			);
		}

		source.clearUpdateNeeded();
		// only usable if compiled
		source.setUsable(compiledOK);

		if (!compiledOK){
			// make sure to dispose id cause all program's
			// shaders will be cleared later.
			logger.info("GLES20.glDeleteShader() ...");
			GLES20.glDeleteShader(id);
			logger.info("GLES20.glDeleteShader() ... done.");
		} else {
			// register for cleanup since the ID is usable
			objManager.registerForCleanup(source);
		}
	}

	protected void resetUniformLocations(Shader shader){
		ListMap<String, Uniform> uniforms = shader.getUniformMap();
	//        for (Uniform uniform : shader.getUniforms()){
		for (int i = 0; i < uniforms.size(); i++){
			Uniform uniform = uniforms.getValue(i);
			uniform.reset(); // e.g check location again
		}
	}

	protected void updateUniform(Shader shader, Uniform uniform) {

		if (verboseLoggingEnabled)
			logger.info("updateUniform(" + shader + ", " + uniform.getName() + ")");

		int shaderId = shader.getId();

		assert uniform.getName() != null;
		assert shader.getId() > 0;

		if (context.boundShaderProgram != shaderId) {
			logger.info("GLES20.glUseProgram(" + shaderId + ") ...");
			GLES20.glUseProgram(shaderId);
			logger.info("GLES20.glUseProgram(" + shaderId + ") ... done.");
			statistics.onShaderUse(shader, true);
			boundShader = shader;
			context.boundShaderProgram = shaderId;
		} else {
			statistics.onShaderUse(shader, false);
		}

		int loc = uniform.getLocation();

		if (loc == -1) {
			logger.warning("uniform.getLocation() is -1.");
			return;
		}

		if (loc == -2) {
			// get uniform location
			logger.warning("uniform.getLocation() is -2.");

			updateUniformLocation(shader, uniform);

			if (uniform.getLocation() == -1) {
			// not declared, ignore
				logger.warning("uniform.getLocation() is -1.");
				uniform.clearUpdateNeeded();
				return;
			}

			loc = uniform.getLocation();
		}

		if (uniform.getVarType() == null) {
			logger.warning("uniform.getVarType() returned null.");
			return; // value not set yet..
		}

		statistics.onUniformSet();

		uniform.clearUpdateNeeded();

		FloatBuffer fb;

		switch (uniform.getVarType()) {
			case Float:
				Float f = (Float) uniform.getValue();
				GLES20.glUniform1f(loc, f.floatValue());
				break;
			case Vector2:
				Vector2f v2 = (Vector2f) uniform.getValue();
				GLES20.glUniform2f(loc, v2.getX(), v2.getY());
				break;
			case Vector3:
				Vector3f v3 = (Vector3f) uniform.getValue();
				GLES20.glUniform3f(loc, v3.getX(), v3.getY(), v3.getZ());
				break;
			case Vector4:
				Object val = uniform.getValue();

				if (val instanceof ColorRGBA){
					ColorRGBA c = (ColorRGBA) val;
					if (verboseLoggingEnabled)
						logger.info("glUniform4f ...");
					GLES20.glUniform4f(loc, c.r, c.g, c.b, c.a);
					if (verboseLoggingEnabled)
						logger.info("glUniform4f ... done.");
				} else {
					Quaternion c = (Quaternion) uniform.getValue();
					if (verboseLoggingEnabled)
						logger.info("glUniform4f ...");
					GLES20.glUniform4f(loc, c.getX(), c.getY(), c.getZ(), c.getW());
					if (verboseLoggingEnabled)
						logger.info("glUniform4f ... done.");
				}
				break;
			case Boolean:
				Boolean b = (Boolean) uniform.getValue();
				GLES20.glUniform1i(loc, b.booleanValue() ? GLES20.GL_TRUE : GLES20.GL_FALSE);
				break;
			case Matrix3:
				fb = (FloatBuffer) uniform.getValue();
				assert fb.remaining() == 9;
				// XXX: count ?
				GLES20.glUniformMatrix3fv(loc,1, false, fb);
				break;
			case Matrix4:
				fb = (FloatBuffer) uniform.getValue();
				assert fb.remaining() == 16;
				// XXX: count ?
				GLES20.glUniformMatrix4fv(loc, 1, false, fb);
				break;
			case FloatArray:
				fb = (FloatBuffer) uniform.getValue();
				// XXX: count ?
				GLES20.glUniform1fv(loc, 1, fb);
				break;
			case Vector2Array:
				fb = (FloatBuffer) uniform.getValue();
				// XXX: count ?
				GLES20.glUniform2fv(loc, 1, fb);
				break;
			case Vector3Array:
				fb = (FloatBuffer) uniform.getValue();
				// XXX: count ?
				GLES20.glUniform3fv(loc, 1, fb);
				break;
			case Vector4Array:
				fb = (FloatBuffer) uniform.getValue();
				// XXX: count ?
				GLES20.glUniform4fv(loc, 1, fb);
				break;
			case Matrix4Array:
				fb = (FloatBuffer) uniform.getValue();
				// XXX: count ?
				GLES20.glUniformMatrix4fv(loc, 1, false, fb);
				break;
			case Int:
				Integer i = (Integer) uniform.getValue();
				GLES20.glUniform1i(loc, i.intValue());
				break;
			default:
				throw new UnsupportedOperationException(
					"Unsupported uniform type: " + uniform.getVarType()
				);
		}
	}

	protected int convertShaderType(ShaderType type) {
		switch (type) {
			case Fragment:
				return GLES20.GL_FRAGMENT_SHADER;
			case Vertex:
				return GLES20.GL_VERTEX_SHADER;
			//            case Geometry:
			//                return ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB;
			default:
				throw new RuntimeException("Unrecognized shader type.");
		}
	}

	private StringBuilder stringBuf = new StringBuilder(250);
	private ByteBuffer nameBuf = BufferUtils.createByteBuffer(250);

	protected void updateUniformLocation(Shader shader, Uniform uniform) {
		logger.info("updateUniformLocation(" + shader + ", " + uniform + ":" + uniform.getName() + ")");

		// XXX: \0 character ?

		logger.info("glGetUniformLocation(" + shader.getId() + ", " + uniform.getName() + ") ...");
		int loc = GLES20.glGetUniformLocation(shader.getId(), uniform.getName());
		logger.info("glGetUniformLocation(" + shader.getId() + ", " + uniform.getName() + ") ... done [" + loc + "]");

		if (loc < 0) {
			uniform.setLocation(-1);
			// uniform is not declared in shader
			logger.warning("Uniform " + uniform.getName() + " is not declared in shader.");
		} else {
			uniform.setLocation(loc);
		}
	}



	/**
	* @param shader The shader to delete. This method also deletes
	* the attached shader sources.
	*/
	public void deleteShader(Shader shader) {

		if (shader.getId() == -1) {
			logger.warning("Shader is not uploaded to GPU, cannot delete.");
			return;
		}

		for (ShaderSource source : shader.getSources()) {
			if (source.getId() != -1) {
				GLES20.glDetachShader(shader.getId(), source.getId());
				// the next part is done by the GLObjectManager automatically
				// glDeleteShader(source.getId());
			}
		}

		// kill all references so sources can be collected
		// if needed.

		shader.resetSources();
		GLES20.glDeleteProgram(shader.getId());

		statistics.onDeleteShader();
	}


	/**
	* Deletes the provided shader source.
	* @param source
	*/
	public void deleteShaderSource(ShaderSource source) {

		if (source.getId() < 0) {
			logger.warning("Shader source is not uploaded to GPU, cannot delete.");
			return;
		}

		source.setUsable(false);
		source.clearUpdateNeeded();
		GLES20.glDeleteShader(source.getId());
		source.resetObject();
	}

	/**
	* Copies contents from src to dst, scaling if neccessary.
	*/
	public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst) {
		logger.warning("copyFrameBuffer: not implemented.");
	//	throw new RendererException("not implemented.");
	}

	/**
	* Sets the framebuffer that will be drawn to.
	*/
	public void setFrameBuffer(FrameBuffer fb) {
		logger.warning("setFrameBuffer: not implemented.");
	//	throw new RendererException("not implemented.");
	}

	/**
	* Reads the pixels currently stored in the specified framebuffer
	* into the given ByteBuffer object. 
	* Only color pixels are transferred, the format is BGRA with 8 bits 
	* per component. The given byte buffer should have at least
	* fb.getWidth() * fb.getHeight() * 4 bytes remaining.
	* @param fb
	* @param byteBuf
	*/
	public void readFrameBuffer(FrameBuffer fb, ByteBuffer byteBuf) {
		logger.warning("readFrameBuffer: not implemented.");
	//	throw new RendererException("not implemented.");
	}

	/**
	* Deletes a framebuffer and all attached renderbuffers
	*/
	public void deleteFrameBuffer(FrameBuffer fb) {
		logger.warning("deleteFrameBuffer: not implemented.");
	//	throw new RendererException("not implemented.");
	}

	/**
	* Sets the texture to use for the given texture unit.
	*/
	public void setTexture(int unit, Texture tex) {
		if (tex.isUpdateNeeded())
			updateTextureData(tex);

		int texId = tex.getId();
		assert texId != -1;

		Texture[] textures = context.boundTextures;

		int type = convertTextureType(tex.getType());

		if (!context.textureIndexList.moveToNew(unit)) {
			if (context.boundTextureUnit != unit) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
				context.boundTextureUnit = unit;
			}
		//             glEnable(type);
		}

		if (textures[unit] != tex) {
			if (context.boundTextureUnit != unit) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
				context.boundTextureUnit = unit;
			}

			GLES20.glBindTexture(type, texId);
			textures[unit] = tex;

			statistics.onTextureUse(tex, true);
		} else {
			statistics.onTextureUse(tex, false);
		}
	}

	private boolean powerOf2 = false;

	public void updateTextureData(Texture tex) {

		int texId = tex.getId();

		if (texId == -1) {
			// create texture
			GLES20.glGenTextures(1, intBuf1);
			texId = intBuf1.get(0);
			tex.setId(texId);
			objManager.registerForCleanup(tex);
		}

		// bind texture
		int target = convertTextureType(tex.getType());

		if (context.boundTextures[0] != tex) {
			if (context.boundTextureUnit != 0) {
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				context.boundTextureUnit = 0;
			}

			GLES20.glBindTexture(target, texId);
			context.boundTextures[0] = tex;
		}

		// filter things
		int minFilter = convertMinFilter(tex.getMinFilter());
		int magFilter = convertMagFilter(tex.getMagFilter());

// XXX?
//		GLES20.glTexParameterx(target, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
//		GLES20.glTexParameterx(target, GLES20.GL_TEXTURE_MAG_FILTER, magFilter);
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
		GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, magFilter);

		// repeat modes
		switch (tex.getType()) {
			case TwoDimensional:
				// XXX?
				//GLES20.glTexParameterx(target, GLES20.GL_TEXTURE_WRAP_T, convertWrapMode(tex.getWrap(WrapAxis.T)));
				GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, convertWrapMode(tex.getWrap(WrapAxis.T)));
				// fall down here is intentional..
			//            case OneDimensional:
				// XXX?
				//GLES20.glTexParameterx(target, GLES20.GL_TEXTURE_WRAP_S, convertWrapMode(tex.getWrap(WrapAxis.S)));
				GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, convertWrapMode(tex.getWrap(WrapAxis.S)));
				break;
			default:
				throw new UnsupportedOperationException("Unknown texture type: " + tex.getType());
		}

		Image img = tex.getImage();

		if (img != null) {
			boolean generateMips = false;
			if (!img.hasMipmaps() && tex.getMinFilter().usesMipMapLevels()) {
				// No pregenerated mips available,
				// generate from base level if required
				// XXX?
				//GLES20.glTexParameterx(target, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_TRUE);
				GLES20.glTexParameteri(target, GLES11.GL_GENERATE_MIPMAP, GLES20.GL_TRUE);
			} else {
				generateMips = true;
			}

			TextureUtil.uploadTexture(gl, img, tex.getImageDataIndex(), generateMips, powerOf2);
		}

		tex.clearUpdateNeeded();
	}

	private int convertWrapMode(Texture.WrapMode mode) {
		switch (mode) {
/*
			case BorderClamp:
				return gl.GL_CLAMP_TO_BORDER;
			case Clamp:
				return gl.GL_CLAMP;
*/
			case EdgeClamp:
				return GLES20.GL_CLAMP_TO_EDGE;
			case Repeat:
				return GLES20.GL_REPEAT;
/*
			case MirroredRepeat:
				return gl.GL_MIRRORED_REPEAT;
*/
			default:
				throw new UnsupportedOperationException("Unknown wrap mode: " + mode);
		}
	}

	private int convertTextureType(Texture.Type type) {
        switch (type){
            case TwoDimensional:
                return GLES20.GL_TEXTURE_2D;
            default:
                throw new UnsupportedOperationException("Unknown texture type: "+type);
        }
    }

	private int convertMagFilter(Texture.MagFilter filter) {
        switch (filter){
            case Bilinear:
                return GLES20.GL_LINEAR;
            case Nearest:
                return GLES20.GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown mag filter: "+filter);
        }
    }

	private int convertMinFilter(Texture.MinFilter filter) {
        switch (filter){
            case Trilinear:
                return GLES20.GL_LINEAR_MIPMAP_LINEAR;
            case BilinearNearestMipMap:
                return GLES20.GL_LINEAR_MIPMAP_NEAREST;
            case NearestLinearMipMap:
                return GLES20.GL_NEAREST_MIPMAP_LINEAR;
            case NearestNearestMipMap:
                return GLES20.GL_NEAREST_MIPMAP_NEAREST;
            case BilinearNoMipMaps:
                return GLES20.GL_LINEAR;
            case NearestNoMipMaps:
                return GLES20.GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown min filter: "+filter);
        }
	}



	/**
	* Deletes a texture from the GPU.
	* @param tex
	*/
	public void deleteTexture(Texture tex) {

		int texId = tex.getId();

		if (texId != -1) {

			intBuf1.put(0, texId);
			intBuf1.position(0).limit(1);
			GLES20.glDeleteTextures(1, intBuf1);
			tex.resetObject();

			statistics.onDeleteTexture();
		}
	}


	/**
	* Uploads a vertex buffer to the GPU.
	* 
	* @param vb The vertex buffer to upload
	*/

	public void updateBufferData(VertexBuffer vb) {
		int bufId = vb.getId();
		boolean created = false;

		if (bufId == -1) {
			// create buffer
			GLES20.glGenBuffers(1, intBuf1);
			bufId = intBuf1.get(0);
			vb.setId(bufId);
			objManager.registerForCleanup(vb);

			created = true;
		}

		// bind buffer

		int target;

		if (vb.getBufferType() == VertexBuffer.Type.Index) {
			target = GLES20.GL_ELEMENT_ARRAY_BUFFER;
			if (context.boundElementArrayVBO != bufId) {
				GLES20.glBindBuffer(target, bufId);
				context.boundElementArrayVBO = bufId;
			}
		} else {
			target = GLES20.GL_ARRAY_BUFFER;
			if (context.boundArrayVBO != bufId) {
				GLES20.glBindBuffer(target, bufId);
				context.boundArrayVBO = bufId;
			}
		}

        int usage = convertUsage(vb.getUsage());
        vb.getData().clear();

        if (created || vb.hasDataSizeChanged()){
            // upload data based on format
		int size = vb.getData().capacity() * vb.getFormat().getComponentSize();
            switch (vb.getFormat()){
                case Byte:
                case UnsignedByte:
                    GLES20.glBufferData(target, size, (ByteBuffer) vb.getData(), usage);
                    break;
    //            case Half:
                case Short:
                case UnsignedShort:
                    GLES20.glBufferData(target, size, (ShortBuffer) vb.getData(), usage);
                    break;
                case Int:
                case UnsignedInt:
                    GLES20.glBufferData(target, size, (IntBuffer) vb.getData(), usage);
                    break;
                case Float:
                    GLES20.glBufferData(target, size, (FloatBuffer) vb.getData(), usage);
                    break;
                case Double:
                    GLES20.glBufferData(target, size, (DoubleBuffer) vb.getData(), usage);
                    break;
                default:
                    throw new RuntimeException("Unknown buffer format.");
            }
        }else{
		int size = vb.getData().capacity() * vb.getFormat().getComponentSize();
            switch (vb.getFormat()){
                case Byte:
                case UnsignedByte:
                    GLES20.glBufferSubData(target, 0, size, (ByteBuffer) vb.getData());
                    break;
                case Short:
                case UnsignedShort:
                    GLES20.glBufferSubData(target, 0, size, (ShortBuffer) vb.getData());
                    break;
                case Int:
                case UnsignedInt:
                    GLES20.glBufferSubData(target, 0, size, (IntBuffer) vb.getData());
                    break;
                case Float:
                    GLES20.glBufferSubData(target, 0, size, (FloatBuffer) vb.getData());
                    break;
                case Double:
                    GLES20.glBufferSubData(target, 0, size, (DoubleBuffer) vb.getData());
                    break;
                default:
                    throw new RuntimeException("Unknown buffer format.");
            }
        }
//        }else{
//            if (created || vb.hasDataSizeChanged()){
//                glBufferData(target, vb.getData().capacity() * vb.getFormat().getComponentSize(), usage);
//            }
//
//            ByteBuffer buf = glMapBuffer(target,
//                                         GL_WRITE_ONLY,
//                                         vb.getMappedData());
//
//            if (buf != vb.getMappedData()){
//                buf = buf.order(ByteOrder.nativeOrder());
//                vb.setMappedData(buf);
//            }
//
//            buf.clear();
//
//            switch (vb.getFormat()){
//                case Byte:
//                case UnsignedByte:
//                    buf.put( (ByteBuffer) vb.getData() );
//                    break;
//                case Short:
//                case UnsignedShort:
//                    buf.asShortBuffer().put( (ShortBuffer) vb.getData() );
//                    break;
//                case Int:
//                case UnsignedInt:
//                    buf.asIntBuffer().put( (IntBuffer) vb.getData() );
//                    break;
//                case Float:
//                    buf.asFloatBuffer().put( (FloatBuffer) vb.getData() );
//                    break;
//                case Double:
//                    break;
//                default:
//                    throw new RuntimeException("Unknown buffer format.");
//            }
//
//            glUnmapBuffer(target);
//        }

        vb.clearUpdateNeeded();
    }

	private int convertUsage(Usage usage) {
        switch (usage){
            case Static:
                return GLES20.GL_STATIC_DRAW;
            case Dynamic:
            case Stream:
                return GLES20.GL_DYNAMIC_DRAW;
            default:
                throw new RuntimeException("Unknown usage type: "+usage);
        }
	}


	/**
	* Deletes a vertex buffer from the GPU.
	* @param vb The vertex buffer to delete
	*/
	public void deleteBuffer(VertexBuffer vb) {
		int bufId = vb.getId();
		if (bufId != -1) {
			// delete buffer
			intBuf1.put(0, bufId);
			intBuf1.position(0).limit(1);
			GLES20.glDeleteBuffers(1, intBuf1);
			vb.resetObject();
		}
	}


    /**
     * Renders <code>count</code> meshes, with the geometry data supplied.
     * The shader which is currently set with <code>setShader</code> is
     * responsible for transforming the input verticies into clip space
     * and shading it based on the given vertex attributes.
     * The int variable gl_InstanceID can be used to access the current
     * instance of the mesh being rendered inside the vertex shader.
     *
     * @param mesh
     * @param count
     */
	public void renderMesh(Mesh mesh, int lod, int count) {

		if (context.pointSize != mesh.getPointSize()){
			GLES10.glPointSize(mesh.getPointSize());
			context.pointSize = mesh.getPointSize();
		}

		if (context.lineWidth != mesh.getLineWidth()){
			GLES20.glLineWidth(mesh.getLineWidth());
			context.lineWidth = mesh.getLineWidth();
		}

		statistics.onMeshDrawn(mesh, lod);

	//        if (GLContext.getCapabilities().GL_ARB_vertex_array_object){
	//            renderMeshVertexArray(mesh, lod, count);
	//        }else{
		renderMeshDefault(mesh, lod, count);
	//        }
	}

	private void renderMeshDefault(Mesh mesh, int lod, int count){
		VertexBuffer indices = null;

		VertexBuffer interleavedData = mesh.getBuffer(Type.InterleavedData);

		if (interleavedData != null && interleavedData.isUpdateNeeded()) {
			updateBufferData(interleavedData);
		}

		IntMap<VertexBuffer> buffers = mesh.getBuffers();

		if (mesh.getNumLodLevels() > 0) {
			indices = mesh.getLodLevel(lod);
		} else {
			indices = buffers.get(Type.Index.ordinal());
		}

		if (verboseLoggingEnabled)
			logger.info("buffers.size: [" + buffers.size() + "]");

		for (Entry<VertexBuffer> entry : buffers) {
			VertexBuffer vb = entry.getValue();
            
			if (
				vb.getBufferType() == Type.InterleavedData ||
				vb.getUsage() == Usage.CpuOnly || // ignore cpu-only buffers
				vb.getBufferType() == Type.Index
			)
				continue;

			if (vb.getStride() == 0){
				// not interleaved
				setVertexAttrib(vb);
			} else {
				// interleaved
				setVertexAttrib(vb, interleavedData);
			}
		}

		if (verboseLoggingEnabled)
			logger.info("indeces " + (indices == null? "==": "!=") + " null.");

		if (indices != null) {
			drawTriangleList(indices, mesh, count);
		} else {

		//            throw new UnsupportedOperationException("Cannot render without index buffer");
			if (verboseLoggingEnabled)
				logger.info("GLES20.glDrawArrays() ...");
			GLES20.glDrawArrays(convertElementMode(mesh.getMode()), 0, mesh.getVertexCount());
			if (verboseLoggingEnabled)
				logger.info("GLES20.glDrawArrays() ... done.");
		}

		clearVertexAttribs();
		clearTextureUnits();
	}


    public void clearTextureUnits(){
        IDList textureList = context.textureIndexList;
        Texture[] textures = context.boundTextures;
        for (int i = 0; i < textureList.oldLen; i++){
            int idx = textureList.oldList[i];

            if (context.boundTextureUnit != idx){
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + idx);
                context.boundTextureUnit = idx;
            }

//            if (textures[idx] == null){
//                System.out.println("!!!");
//            }

//            glDisable(convertTextureType(textures[idx].getType()));
            textures[idx] = null;
        }
        context.textureIndexList.copyNewToOld();
    }



    protected void clearVertexAttribs(){
        IDList attribList = context.attribIndexList;
        for (int i = 0; i < attribList.oldLen; i++){
            int idx = attribList.oldList[i];
            GLES20.glDisableVertexAttribArray(idx);
            context.boundAttribs[idx] = null;
        }
        context.attribIndexList.copyNewToOld();
    }

    protected void setVertexAttrib(VertexBuffer vb, VertexBuffer idb){

		if (verboseLoggingEnabled)
			logger.info("setVertexAttribute()");

        if (vb.getBufferType() == VertexBuffer.Type.Index)
            throw new IllegalArgumentException("Index buffers not allowed to be set to vertex attrib");

        if (vb.isUpdateNeeded() && idb == null)
            updateBufferData(vb);

        int programId = context.boundShaderProgram;
        if (programId > 0){
            Attribute attrib = boundShader.getAttribute(vb.getBufferType().name());
            int loc = attrib.getLocation();
		if (loc == -1) {
			logger.warning("attrib.getLocation is -1: [" + vb.getBufferType().name() + "]");
			return; // not deinfod
		}

            if (loc == -2){

		String attributeName = "in" + vb.getBufferType().name();

		logger.info("GLES20.glGetAttribLocation(" + programId + ", " + attributeName + ") ...");
                loc = GLES20.glGetAttribLocation(programId, attributeName);
		logger.info("GLES20.glGetAttribLocation(" + programId + ", " + attributeName + ") ... done [" + loc + "].");

                // not really the name of it in the shader (inPosition\0) but
                // the internal name of the enum (Position).
                if (loc < 0){
                    attrib.setLocation(-1);
			logger.warning("GLES20.glGetAttribLocation returned " + loc + ".");
                    return; // not available in shader.
                }else{
                    attrib.setLocation(loc);
                }
            }

            VertexBuffer[] attribs = context.boundAttribs;
            if (!context.attribIndexList.moveToNew(loc)){
		logger.info("GLES20.glEnableVertexAttribArray(" + loc + ") ...");
                GLES20.glEnableVertexAttribArray(loc);
		logger.info("GLES20.glEnableVertexAttribArray(" + loc + ") ... done.");
                //System.out.println("Enabled ATTRIB IDX: "+loc);
            }
            if (attribs[loc] != vb){
                // NOTE: Use id from interleaved buffer if specified
                int bufId = idb != null ? idb.getId() : vb.getId();
                assert bufId != -1;
                if (context.boundArrayVBO != bufId){
			logger.info("GLES20.glBindBuffer() ...");
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufId);
			logger.info("GLES20.glBindBuffer() ... done.");
                    context.boundArrayVBO = bufId;
                }

	// XXX

		vb.getData().clear();

		GLES20.glVertexAttribPointer(
			loc,
			vb.getNumComponents(),
			convertFormat(vb.getFormat()),
			vb.isNormalized(),
			vb.getStride(),
			vb.getData()
		);

                attribs[loc] = vb;
            }
        }else{
            throw new IllegalStateException("Cannot render mesh without shader bound");
        }
    }

    protected void setVertexAttrib(VertexBuffer vb){
        setVertexAttrib(vb, null);
    }


    public void drawTriangleList(VertexBuffer indexBuf, Mesh mesh, int count) {
        Mesh.Mode mode = mesh.getMode();

        Buffer indexData = indexBuf.getData();
        indexData.clear();
        if (mesh.getMode() == Mode.Hybrid){
            int[] modeStart      = mesh.getModeStart();
            int[] elementLengths = mesh.getElementLengths();

            int elMode = convertElementMode(Mode.Triangles);
            int fmt    = convertVertexFormat(indexBuf.getFormat());
//            int elSize = indexBuf.getFormat().getComponentSize();
//            int listStart = modeStart[0];
            int stripStart = modeStart[1];
            int fanStart = modeStart[2];
            int curOffset = 0;
            for (int i = 0; i < elementLengths.length; i++){
                if (i == stripStart){
                    elMode = convertElementMode(Mode.TriangleStrip);
                }else if (i == fanStart){
                    elMode = convertElementMode(Mode.TriangleStrip);
                }
                int elementLength = elementLengths[i];
                indexData.position(curOffset);
		if (verboseLoggingEnabled)
			logger.info("GLES20.glDrawElements() 0 ...");
                GLES20.glDrawElements(elMode,
                                  elementLength,
                                  fmt,
                                  indexData);
                curOffset += elementLength;
		if (verboseLoggingEnabled)
			logger.info("GLES20.glDrawElements() 0 ... done.");
            }
        }else{
		if (verboseLoggingEnabled)
			logger.info("GLES20.glDrawElements() 1...");
		GLES20.glDrawElements(convertElementMode(mode),
                              indexData.capacity(),
                              convertVertexFormat(indexBuf.getFormat()),
                              indexData);
		if (verboseLoggingEnabled)
			logger.info("GLES20.glDrawElements() 1 ... done.");
        }
    }

    private int convertElementMode(Mesh.Mode mode){
        switch (mode){
            case Points:
                return GLES20.GL_POINTS;
            case Lines:
                return GLES20.GL_LINES;
            case LineLoop:
                return GLES20.GL_LINE_LOOP;
            case LineStrip:
                return GLES20.GL_LINE_STRIP;
            case Triangles:
                return GLES20.GL_TRIANGLES;
            case TriangleFan:
                return GLES20.GL_TRIANGLE_FAN;
            case TriangleStrip:
                return GLES20.GL_TRIANGLE_STRIP;
            default:
                throw new UnsupportedOperationException("Unrecognized mesh mode: "+mode);
        }
    }


    private int convertFormat(Format format){
        switch (format){
            case Byte:
                return GLES20.GL_BYTE;
            case UnsignedByte:
                return GLES20.GL_UNSIGNED_BYTE;
            case Short:
                return GLES20.GL_SHORT;
            case UnsignedShort:
                return GLES20.GL_UNSIGNED_SHORT;
            case Int:
                return GLES20.GL_INT;
            case UnsignedInt:
                return GLES20.GL_UNSIGNED_INT;
            case Float:
                return GLES20.GL_FLOAT;
         //   case Double:
          //      return GLES20.GL_DOUBLE;
            default:
                throw new RuntimeException("Unknown buffer format.");

        }
    }

    private int convertVertexFormat(VertexBuffer.Format fmt){
        switch (fmt){
            case Byte:
                return GLES20.GL_BYTE;
            case Float:
                return GLES20.GL_FLOAT;
            case Short:
                return GLES20.GL_SHORT;
            case UnsignedByte:
                return GLES20.GL_UNSIGNED_BYTE;
            case UnsignedShort:
                return GLES20.GL_UNSIGNED_SHORT;
            case Int:
                return GLES20.GL_FIXED;
            default:
                throw new UnsupportedOperationException("Unrecognized vertex format: "+fmt);
        }
    }



    /**
     * Called on restart() to reset all GL objects
     */
    public void resetGLObjects(){
        objManager.resetObjects();
        statistics.clearMemory();
        boundShader = null;
       // lastFb = null;
        context.reset();
    }


    /**
     * Called when the display is restarted to delete
     * all created GL objects.
     */
       public void cleanup(){
        objManager.deleteAllObjects(this);
        statistics.clearMemory();
    }


	public void initialize(){
		logger.info("Vendor: "+GLES20.glGetString(GLES20.GL_VENDOR));
		logger.info("Renderer: "+GLES20.glGetString(GLES20.GL_RENDERER));
		logger.info("Version: "+GLES20.glGetString(GLES20.GL_VERSION));

		String shadingLanguageVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
		logger.info("GLES20.Shading Language Version: " + shadingLanguageVersion);

	//	caps.add(Caps.GLSL110);
		caps.add(Caps.GLSL100);

		String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		logger.info("GLES20.extensions: " + extensions);
		extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		logger.info("GLES20.extensions: " + extensions);

/*
        ContextCapabilities ctxCaps = GLContext.getCapabilities();
        if (ctxCaps.OpenGL20){
            caps.add(Caps.OpenGL20);
        }
        if (ctxCaps.OpenGL21){
            caps.add(Caps.OpenGL21);
        }
        if (ctxCaps.OpenGL30){
            caps.add(Caps.OpenGL30);
        }

        String versionStr = glGetString(GL_SHADING_LANGUAGE_VERSION);
        if (versionStr == null || versionStr.equals("")){
            glslVer = -1;
            throw new UnsupportedOperationException("GLSL and OpenGL2 is " +
                                                    "required for the LWJGL " +
                                                    "renderer!");
        }

        // Fix issue in TestRenderToMemory when GL_FRONT is the main
        // buffer being used.
        initialDrawBuf = glGetInteger(GL_DRAW_BUFFER);
        initialReadBuf = glGetInteger(GL_READ_BUFFER);

        int spaceIdx = versionStr.indexOf(" ");
        if (spaceIdx >= 1)
            versionStr = versionStr.substring(0, spaceIdx);

        float version = Float.parseFloat(versionStr);
        glslVer = (int) (version * 100);
        
        switch (glslVer){
            default:
                if (glslVer < 400)
                    break;

                // so that future OpenGL revisions wont break jme3

                // fall through intentional
            case 400:
            case 330:
            case 150:
                caps.add(Caps.GLSL150);
            case 140:
                caps.add(Caps.GLSL140);
            case 130:
                caps.add(Caps.GLSL130);
            case 120:
                caps.add(Caps.GLSL120);
            case 110:
                caps.add(Caps.GLSL110);
            case 100:
                caps.add(Caps.GLSL100);
                break;
        }

        if (!caps.contains(Caps.GLSL100)){
            logger.log(Level.WARNING, "Force-adding GLSL100 support, since OpenGL is supported.");
            caps.add(Caps.GLSL100);
        }

        glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, intBuf16);
        vertexTextureUnits = intBuf16.get(0);
        logger.log(Level.FINER, "VTF Units: {0}", vertexTextureUnits);
        if (vertexTextureUnits > 0)
            caps.add(Caps.VertexTextureFetch);

        glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS, intBuf16);
        fragTextureUnits = intBuf16.get(0);
        logger.log(Level.FINER, "Texture Units: {0}", fragTextureUnits);

        glGetInteger(GL_MAX_VERTEX_UNIFORM_COMPONENTS, intBuf16);
        vertexUniforms = intBuf16.get(0);
        logger.log(Level.FINER, "Vertex Uniforms: {0}", vertexUniforms);

        glGetInteger(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, intBuf16);
        fragUniforms = intBuf16.get(0);
        logger.log(Level.FINER, "Fragment Uniforms: {0}", fragUniforms);
        
        glGetInteger(GL_MAX_VERTEX_ATTRIBS, intBuf16);
        vertexAttribs = intBuf16.get(0);
        logger.log(Level.FINER, "Vertex Attributes: {0}", vertexAttribs);

        glGetInteger(GL_MAX_VARYING_FLOATS, intBuf16);
        int varyingFloats = intBuf16.get(0);
        logger.log(Level.FINER, "Varying Floats: {0}", varyingFloats);

        glGetInteger(GL_SUBPIXEL_BITS, intBuf16);
        int subpixelBits  = intBuf16.get(0);
        logger.log(Level.FINER, "Subpixel Bits: {0}", subpixelBits);

        glGetInteger(GL_MAX_ELEMENTS_VERTICES, intBuf16);
        maxVertCount = intBuf16.get(0);
        logger.log(Level.FINER, "Preferred Batch Vertex Count: {0}", maxVertCount);
        
        glGetInteger(GL_MAX_ELEMENTS_INDICES, intBuf16);
        maxTriCount = intBuf16.get(0);
        logger.log(Level.FINER, "Preferred Batch Index Count: {0}", maxTriCount);

        glGetInteger(GL_MAX_TEXTURE_SIZE, intBuf16);
        maxTexSize = intBuf16.get(0);
        logger.log(Level.FINER, "Maximum Texture Resolution: {0}", maxTexSize);

        glGetInteger(GL_MAX_CUBE_MAP_TEXTURE_SIZE, intBuf16);
        maxCubeTexSize = intBuf16.get(0);
        logger.log(Level.FINER, "Maximum CubeMap Resolution: {0}", maxCubeTexSize);

        if (ctxCaps.GL_ARB_color_buffer_float){
            // XXX: Require both 16 and 32 bit float support for FloatColorBuffer.
            if (ctxCaps.GL_ARB_half_float_pixel){
                caps.add(Caps.FloatColorBuffer);
            }
        }

        if (ctxCaps.GL_ARB_depth_buffer_float){
            caps.add(Caps.FloatDepthBuffer);
        }

        if (ctxCaps.GL_ARB_draw_instanced)
            caps.add(Caps.MeshInstancing);

        if (ctxCaps.GL_ARB_fragment_program)
            caps.add(Caps.ARBprogram);

        if (ctxCaps.GL_ARB_texture_buffer_object)
            caps.add(Caps.TextureBuffer);

        if (ctxCaps.GL_ARB_texture_float){
            if (ctxCaps.GL_ARB_half_float_pixel){
                caps.add(Caps.FloatTexture);
            }
        }

        if (ctxCaps.GL_ARB_vertex_array_object)
            caps.add(Caps.VertexBufferArray);

        boolean latc = ctxCaps.GL_EXT_texture_compression_latc;
        boolean atdc = ctxCaps.GL_ATI_texture_compression_3dc;
        if (latc || atdc){
            caps.add(Caps.TextureCompressionLATC);
            if (atdc && !latc){
                tdc = true;
            }
        }

        if (ctxCaps.GL_EXT_packed_float){
            caps.add(Caps.PackedFloatColorBuffer);
            if (ctxCaps.GL_ARB_half_float_pixel){
                // because textures are usually uploaded as RGB16F
                // need half-float pixel
                caps.add(Caps.PackedFloatTexture);
            }
        }

        if (ctxCaps.GL_EXT_texture_array)
            caps.add(Caps.TextureArray);

        if (ctxCaps.GL_EXT_texture_shared_exponent)
            caps.add(Caps.SharedExponentTexture);

        if (ctxCaps.GL_EXT_framebuffer_object){
            caps.add(Caps.FrameBuffer);

            glGetInteger(GL_MAX_RENDERBUFFER_SIZE_EXT, intBuf16);
            maxRBSize = intBuf16.get(0);
            logger.log(Level.FINER, "FBO RB Max Size: {0}", maxRBSize);

            glGetInteger(GL_MAX_COLOR_ATTACHMENTS_EXT, intBuf16);
            maxFBOAttachs = intBuf16.get(0);
            logger.log(Level.FINER, "FBO Max renderbuffers: {0}", maxFBOAttachs);

            if (ctxCaps.GL_EXT_framebuffer_multisample){
                caps.add(Caps.FrameBufferMultisample);

                glGetInteger(GL_MAX_SAMPLES_EXT, intBuf16);
                maxFBOSamples = intBuf16.get(0);
                logger.log(Level.FINER, "FBO Max Samples: {0}", maxFBOSamples);
            }

            if (ctxCaps.GL_ARB_draw_buffers){
                caps.add(Caps.FrameBufferMRT);
                glGetInteger(ARBDrawBuffers.GL_MAX_DRAW_BUFFERS_ARB, intBuf16);
                maxMRTFBOAttachs = intBuf16.get(0);
                logger.log(Level.FINER, "FBO Max MRT renderbuffers: {0}", maxMRTFBOAttachs);
            }
        }

        if (ctxCaps.GL_ARB_multisample){
            glGetInteger(ARBMultisample.GL_SAMPLE_BUFFERS_ARB, intBuf16);
            boolean available = intBuf16.get(0) != 0;
            glGetInteger(ARBMultisample.GL_SAMPLES_ARB, intBuf16);
            int samples = intBuf16.get(0);
            logger.log(Level.FINER, "Samples: {0}", samples);
            boolean enabled = glIsEnabled(ARBMultisample.GL_MULTISAMPLE_ARB);
            if (samples > 0 && available && !enabled){
                glEnable(ARBMultisample.GL_MULTISAMPLE_ARB);
            }
        }
	*/

        extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (extensions.contains("GL_OES_texture_npot"))
            powerOf2 = true;
        
        applyRenderState(RenderState.DEFAULT);
//        GLES20.glClearDepthf(1.0f);
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        logger.log(Level.INFO, "Caps: " + caps);
    }


 
}

/*
 * Copyright (c) 2003-2009 jMonkeyEngine
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

package com.jmex.effects;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

//import org.lwjgl.opengl.GL11;
//import org.lwjgl.util.glu.GLU;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GL;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.scene.state.jogl.records.RendererRecord;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.gl2.GLUgl2;

/**
 * <code>ProjectedTextureUtil</code>
 * 
 * @author Rikard Herlitz (MrCoder)
 */
public class ProjectedTextureUtil {
    private static Matrix4f lightProjectionMatrix = new Matrix4f();
    private static Matrix4f lightViewMatrix = new Matrix4f();
    private static Matrix4f biasMatrix = new Matrix4f(0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.5f, 0.5f, 0.5f,
            1.0f); // bias from [-1, 1] to [0, 1]

	/**
	 * Sets the provided texture up for projection use
	 * @param texture Texture to use for projection
	 * @param wrapMode Wrapping mode
	 * @param combineMode Combine mode
	public static void setupProjectedTexture( Texture texture, Texture.WrapMode wrapMode, Texture.CombinerFunctionRGB combineMode ) {
		texture.setEnvironmentalMapMode( Texture.EnvironmentalMapMode.EyeLinear );

		texture.setApply( Texture.ApplyMode.Combine );
		texture.setCombineSrc0RGB( Texture.CombinerSource.CurrentTexture );
		texture.setCombineOp0RGB( Texture.CombinerOperandRGB.SourceColor );
		texture.setCombineSrc1RGB( Texture.CombinerSource.Previous );
		texture.setCombineOp1RGB( Texture.CombinerOperandRGB.SourceColor );
		texture.setCombineScaleRGB( Texture.CombinerScale.One );

    /**
     * Updated texture matrix on the provided texture
     * 
     * @param texture
     *            Texture to update texturematrix on
     * @param fov
     *            Projector field of view, in angles
     * @param aspect
     *            Projector frustum aspect ratio
     * @param near
     *            Projector frustum near plane
     * @param far
     *            Projector frustum far plane
     * @param pos
     *            Projector position
     * @param aim
     *            Projector look at position
     */
    public static void updateProjectedTexture(Texture texture, float fov,
            float aspect, float near, float far, Vector3f pos, Vector3f aim,
            Vector3f up) {
        matrixPerspective(fov, aspect, near, far, lightProjectionMatrix);
        matrixLookAt(pos, aim, up, lightViewMatrix);
        texture.getMatrix().set(
                lightViewMatrix.multLocal(lightProjectionMatrix).multLocal(
                        biasMatrix)).transposeLocal();
    }

    // UTILS
    private static final FloatBuffer tmp_FloatBuffer = BufferUtils
            .createFloatBuffer(16);
    private static Vector3f localDir = new Vector3f();
    private static Vector3f localLeft = new Vector3f();
    private static Vector3f localUp = new Vector3f();

    private static IntBuffer matrixModeBuffer = BufferUtils.createIntBuffer(16);
    private static int savedMatrixMode = 0;
    private static GLU glu = new GLUgl2();

    private static void saveMatrixMode() {
        GL gl = glu.getCurrentGL();
        
        matrixModeBuffer.rewind();
        gl.glGetIntegerv(GL2.GL_MATRIX_MODE, matrixModeBuffer);
        savedMatrixMode = matrixModeBuffer.get(0);
    }

    private static void restoreMatrixMode() {
        RendererRecord matRecord = (RendererRecord) DisplaySystem
                .getDisplaySystem().getCurrentContext().getRendererRecord();
        matRecord.switchMode(savedMatrixMode);
    }

    public static void matrixLookAt(Vector3f location, Vector3f at,
            Vector3f up, Matrix4f result) {
        GL2 gl = glu.getCurrentGL().getGL2();
                
        localDir.set(at).subtractLocal(location).normalizeLocal();
        localDir.cross(up, localLeft);
        localLeft.cross(localDir, localUp);

        saveMatrixMode();

        // set view matrix
        RendererRecord matRecord = (RendererRecord) DisplaySystem
                .getDisplaySystem().getCurrentContext().getRendererRecord();
        matRecord.switchMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluLookAt(location.x, location.y, location.z, at.x, at.y, at.z,
                localUp.x, localUp.y, localUp.z);

        if (result != null) {
            tmp_FloatBuffer.rewind();
            gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, tmp_FloatBuffer);
            tmp_FloatBuffer.rewind();
            result.readFloatBuffer(tmp_FloatBuffer);
        }

        gl.glPopMatrix();
        restoreMatrixMode();
    }

    public static void matrixPerspective(float fovY, float aspect, float near,
            float far, Matrix4f result) {
        GL2 gl = glu.getCurrentGL().getGL2();
        saveMatrixMode();

        // set view matrix
        RendererRecord matRecord = (RendererRecord) DisplaySystem
                .getDisplaySystem().getCurrentContext().getRendererRecord();
        matRecord.switchMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluPerspective(fovY, aspect, near, far);

        if (result != null) {
            tmp_FloatBuffer.rewind();
            gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, tmp_FloatBuffer);
            tmp_FloatBuffer.rewind();
            result.readFloatBuffer(tmp_FloatBuffer);
        }

        gl.glPopMatrix();
        restoreMatrixMode();
    }

    public static void matrixProjection(float fovY, float aspect, float near,
            float far, Matrix4f result) {
        GL2 gl = glu.getCurrentGL().getGL2();
        float h = FastMath.tan(fovY * FastMath.DEG_TO_RAD * .5f) * near;
        float w = h * aspect;
        float frustumLeft = -w;
        float frustumRight = w;
        float frustumBottom = -h;
        float frustumTop = h;
        float frustumNear = near;
        float frustumFar = far;

        saveMatrixMode();
        RendererRecord matRecord = (RendererRecord) DisplaySystem
                .getDisplaySystem().getCurrentContext().getRendererRecord();
        matRecord.switchMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glFrustum(frustumLeft, frustumRight, frustumBottom, frustumTop,
                frustumNear, frustumFar);

        if (result != null) {
            tmp_FloatBuffer.rewind();
            gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, tmp_FloatBuffer);
            tmp_FloatBuffer.rewind();
            result.readFloatBuffer(tmp_FloatBuffer);
        }

        gl.glPopMatrix();
        restoreMatrixMode();
    }

    public static void matrixFrustum(float frustumLeft, float frustumRight,
            float frustumBottom, float frustumTop, float frustumNear,
            float frustumFar, Matrix4f result) {
        GL2 gl = glu.getCurrentGL().getGL2();
        saveMatrixMode();
        RendererRecord matRecord = (RendererRecord) DisplaySystem
                .getDisplaySystem().getCurrentContext().getRendererRecord();
        matRecord.switchMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glFrustum(frustumLeft, frustumRight, frustumBottom, frustumTop,
                frustumNear, frustumFar);

        if (result != null) {
            tmp_FloatBuffer.rewind();
            gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, tmp_FloatBuffer);
            tmp_FloatBuffer.rewind();
            result.readFloatBuffer(tmp_FloatBuffer);
        }

        gl.glPopMatrix();
        restoreMatrixMode();
    }
}

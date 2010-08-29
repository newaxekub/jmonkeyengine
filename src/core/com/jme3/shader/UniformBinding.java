package com.jme3.shader;

public enum UniformBinding {

    /**
     * The world matrix. Converts Model space to World space.
     * Type: mat4
     */
    WorldMatrix,

    /**
     * The view matrix. Converts World space to View space.
     * Type: mat4
     */
    ViewMatrix,

    /**
     * The projection matrix. Converts View space to Clip/Projection space.
     * Type: mat4
     */
    ProjectionMatrix,

    /**
     * The world view matrix. Converts Model space to View space.
     * Type: mat4
     */
    WorldViewMatrix,

    /**
     * The normal matrix. The inverse transpose of the worldview matrix.
     * Converts normals from model space to view space.
     * Type: mat3
     */
    NormalMatrix,

    /**
     * The world view projection matrix. Converts Model space to Clip/Projection
     * space.
     * Type: mat4
     */
    WorldViewProjectionMatrix,

    WorldMatrixInverse,
    ViewMatrixInverse,
    ProjectionMatrixInverse,
    WorldViewMatrixInverse,
    NormalMatrixInverse,
    WorldViewProjectionMatrixInverse,

    /**
     * Contains the four viewport parameters in this order:
     * X = Left,
     * Y = Top,
     * Z = Right,
     * W = Bottom.
     * Type: vec4
     */
    ViewPort,

    /**
     * The near and far values for the camera frustum.
     * X = Near
     * Y = Far.
     * Type: vec2
     */
    FrustumNearFar,
    
    /**
     * The width and height of the camera.
     * Type: vec2
     */
    Resolution,

    /**
     * Aspect ratio of the resolution currently set. Width/Height.
     * Type: float
     */
    Aspect,

    /**
     * Camera position in world space.
     * Type: vec3
     */
    CameraPosition,

    /**
     * Direction of the camera.
     * Type: vec3
     */
    CameraDirection,

    /**
     * Left vector of the camera.
     * Type: vec3
     */
    CameraLeft,

    /**
     * Up vector of the camera.
     * Type: vec3
     */
    CameraUp,

    /**
     * Time in seconds since the application was started.
     * Type: float
     */
    Time,

    /**
     * Time in seconds that the last frame took.
     * Type: float
     */
    Tpf,

    /**
     * Frames per second.
     * Type: float
     */
    FrameRate;
}

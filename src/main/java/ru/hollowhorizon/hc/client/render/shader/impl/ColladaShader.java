package ru.hollowhorizon.hc.client.render.shader.impl;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.render.shader.ShaderProgram;
import ru.hollowhorizon.hc.client.render.shader.uniforms.*;

import java.io.IOException;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ColladaShader extends ShaderProgram {
    private final UniformMatrix.Mat4 modelViewMatrix = new UniformMatrix.Mat4("modelViewMatrix");
    private final UniformMatrix.Mat4 projectionMatrix = new UniformMatrix.Mat4("projectionMatrix");
    private final UniformArray.Mat4 jointTransforms = new UniformArray.Mat4("jointTransforms", 50);
public static ColladaShader SHADER;
    private final UniformPrimitive.Sampler textureSampler = new UniformPrimitive.Sampler("textureSampler");
    private final UniformPrimitive.Sampler overlaySampler = new UniformPrimitive.Sampler("overlaySampler");
    private final UniformPrimitive.Sampler lightmapSampler = new UniformPrimitive.Sampler("lightmapSampler");

    private final UniformVector.Vec2 lightMapTextureCoords = new UniformVector.Vec2("lightMapTextureCoords");
    private final UniformVector.Vec2 overlayTextureCoords = new UniformVector.Vec2("overlayTextureCoords");

    public ColladaShader() throws IOException
    {
        super(
                new ResourceLocation(MODID, "shaders/animatrix_vertex.glsl"), new ResourceLocation(MODID, "shaders/animatrix_fragment.glsl"),
                "in_position", "in_textureCoords", "in_normal", "in_jointIndices", "in_weights");
        super.storeAllUniformLocations(
                modelViewMatrix,
                projectionMatrix,
                jointTransforms,
                textureSampler,
                overlaySampler,
                lightmapSampler,
                lightMapTextureCoords,
                overlayTextureCoords
        );
    }

    public static void init() throws IOException {
        SHADER = new ColladaShader();
    }

    public UniformMatrix.Mat4 getModelViewMatrix() {
        return modelViewMatrix;
    }

    public UniformMatrix.Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public UniformArray.Mat4 getJointTransforms() {
        return jointTransforms;
    }

    public UniformVector.Vec2 getLightMapTextureCoords() {
        return lightMapTextureCoords;
    }

    public UniformVector.Vec2 getOverlayTextureCoords() {
        return overlayTextureCoords;
    }

    public UniformPrimitive.Sampler getTextureSampler() {
        return textureSampler;
    }

    public UniformPrimitive.Sampler getOverlaySampler() {
        return overlaySampler;
    }

    public UniformPrimitive.Sampler getLightmapSampler() {
        return lightmapSampler;
    }
}

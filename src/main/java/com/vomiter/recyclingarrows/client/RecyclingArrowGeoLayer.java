package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RecyclingArrowGeoLayer<T extends Entity & GeoAnimatable>
        extends GeoRenderLayer<T> {

    private final EntityRenderDispatcher entityRenderDispatcher;

    public RecyclingArrowGeoLayer(GeoRenderer<T> renderer,
                                  EntityRenderDispatcher entityRenderDispatcher) {
        super(renderer);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack,
                       T animatable,
                       BakedGeoModel bakedModel,
                       RenderType renderType,
                       MultiBufferSource bufferSource,
                       VertexConsumer buffer,
                       float partialTick,
                       int packedLight,
                       int packedOverlay) {
        RecyclingArrowRenderHelper.render(
                poseStack,
                bufferSource,
                packedLight,
                animatable,
                partialTick,
                entityRenderDispatcher,
                true
        );
    }
}
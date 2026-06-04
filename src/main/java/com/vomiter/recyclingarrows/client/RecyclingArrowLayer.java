package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class RecyclingArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    public RecyclingArrowLayer(RenderLayerParent<T, M> parent, EntityRenderDispatcher entityRenderDispatcher) {
        super(parent);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       @NotNull T entity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTick,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        RecyclingArrowRenderHelper.render(
                poseStack,
                buffer,
                packedLight,
                entity,
                partialTick,
                entityRenderDispatcher
        );
    }
}
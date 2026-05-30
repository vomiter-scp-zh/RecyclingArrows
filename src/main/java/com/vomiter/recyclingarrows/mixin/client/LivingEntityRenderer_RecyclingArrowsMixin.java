package com.vomiter.recyclingarrows.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vomiter.recyclingarrows.client.RecyclingArrowRenderContexts;
import com.vomiter.recyclingarrows.client.RecyclingArrowRenderData;
import com.vomiter.recyclingarrows.client.RecyclingArrowSubmitHelper;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderer_RecyclingArrowsMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void recyclingarrows$extractArrowRenderData(
            LivingEntity entity,
            LivingEntityRenderState state,
            float partialTicks,
            CallbackInfo ci
    ) {
        var holder = EntityArrowStorageAccess.getNullable(entity);
        RecyclingArrowRenderData data =
                RecyclingArrowRenderData.from(entity, holder.getArrows());
        RecyclingArrowRenderContexts.ARROWS_DATA.put(state, data);

    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"
            )
    )
    private void recyclingarrows$submitAttachedArrows(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        RecyclingArrowSubmitHelper.submit(state, poseStack, submitNodeCollector, camera);
    }
}
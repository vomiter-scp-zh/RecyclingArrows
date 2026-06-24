package com.vomiter.recyclingarrows.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerRenderer.class, remap = false)
public class PlayerRendererMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"))
    private boolean recyclingarrows$skipVanillaArrowLayer(PlayerRenderer instance, RenderLayer renderLayer, Operation<Boolean> original){
        if(renderLayer instanceof ArrowLayer<?,?>) return false;
        return original.call(instance, renderLayer);
    }
}

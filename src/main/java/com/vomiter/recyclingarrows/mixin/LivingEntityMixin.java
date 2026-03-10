package com.vomiter.recyclingarrows.mixin;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void dropAllArrows(DamageSource damageSource, CallbackInfo ci){
        var self = (LivingEntity)(Object)this;
        RecyclingArrows
                .ARROW_HIT_SERVICE
                .getArrows(self)
                .forEach(self::spawnAtLocation);
    }
}

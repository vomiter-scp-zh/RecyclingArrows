package com.vomiter.recyclingarrows.mixin.compat;

import com.oblivioussp.spartanweaponry.entity.projectile.BoltEntity;
import com.vomiter.recyclingarrows.common.arrow.logic.ArrowHitService;
import com.vomiter.recyclingarrows.common.arrow.logic.IArrowAccessor;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoltEntity.class)
public abstract class SpartanBoltMixin implements IArrowAccessor {
    @Shadow
    protected abstract ItemStack getPickupItem();

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lcom/oblivioussp/spartanweaponry/entity/projectile/BoltEntity;discard()V", ordinal = 0))
    private void addArrow1(EntityHitResult hit, CallbackInfo ci){
        ArrowHitService.addArrow(hit, (AbstractArrow)(Object)this, recyclingarrows$getItem());
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lcom/oblivioussp/spartanweaponry/entity/projectile/BoltEntity;discard()V", ordinal = 1))
    private void addArrow2(EntityHitResult hit, CallbackInfo ci){
        ArrowHitService.addArrow(hit, (AbstractArrow)(Object)this, recyclingarrows$getItem());
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lcom/oblivioussp/spartanweaponry/entity/projectile/BoltEntity;discard()V", ordinal = 2))
    private void addArrow3(EntityHitResult hit, CallbackInfo ci){
        ArrowHitService.addArrow(hit, (AbstractArrow)(Object)this, recyclingarrows$getItem());
    }


    @Override
    public ItemStack recyclingarrows$getItem() {
        return getPickupItem();
    }


}

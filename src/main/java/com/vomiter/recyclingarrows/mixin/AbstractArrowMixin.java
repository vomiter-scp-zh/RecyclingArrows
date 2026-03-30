package com.vomiter.recyclingarrows.mixin;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.logic.IArrowAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements IArrowAccessor {
    @Shadow
    protected abstract ItemStack getPickupItem();

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V", ordinal = 0))
    private void addArrow1(EntityHitResult hit, CallbackInfo ci){
        recyclingArrows$addArrow(hit);
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V", ordinal = 1))
    private void addArrow2(EntityHitResult hit, CallbackInfo ci){
        recyclingArrows$addArrow(hit);
    }

    @Unique
    void recyclingArrows$addArrow(EntityHitResult hit){
        Entity entity = hit.getEntity();
        if(entity instanceof LivingEntity living){
            RecyclingArrows.LOGGER.debug("Add arrow {} to {}", getPickupItem(), living);
            if(living.isAlive()){
                RecyclingArrows.ARROW_HIT_SERVICE.recordArrowHit((AbstractArrow)(Object) this, living, hit);
                living.level().getEntitiesOfClass(ServerPlayer.class, living.getBoundingBox().inflate(64)).forEach(serverPlayer -> {
                    RecyclingArrows.arrowSyncService.syncToPlayer(living, serverPlayer);
                });
            }
            else{
                living.spawnAtLocation(getPickupItem());
            }
        }
    }

    @Override
    public ItemStack recyclingarrows$getItem() {
        return getPickupItem();
    }

}

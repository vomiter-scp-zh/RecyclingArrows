package com.vomiter.recyclingarrows.mixin.compat;

import com.oblivioussp.spartanweaponry.entity.projectile.BoltEntity;
import com.oblivioussp.spartanweaponry.item.BoltItem;
import com.vomiter.recyclingarrows.common.arrow.logic.IArrowItemAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoltItem.class)
public abstract class SpartanBoltItemMixin implements IArrowItemAccessor {
    @Shadow
    public abstract BoltEntity createBolt(Level level, ItemStack stack, LivingEntity shooter);

    @Override
    public AbstractArrow recyclingarrows$getArrowEntity(Level level, ItemStack stack, LivingEntity owner) {
        return createBolt(level, stack, owner);
    }
}

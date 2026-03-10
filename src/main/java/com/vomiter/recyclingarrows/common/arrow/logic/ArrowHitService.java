package com.vomiter.recyclingarrows.common.arrow.logic;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.IEntityArrowStorageAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArrowHitService {
    private final IEntityArrowStorageAccess storageAccess;

    public ArrowHitService(IEntityArrowStorageAccess storageAccess) {
        this.storageAccess = storageAccess;
    }

    public void recordArrowHit(AbstractArrow arrow, LivingEntity target) {
        if (arrow == null || target == null) {
            return;
        }

        StoredArrow stored = ArrowItemResolver.resolve(arrow);
        RecyclingArrows.LOGGER.debug("resolved arrow = {}", stored);

        if (stored == null) {
            return;
        }

        IArrowRecordHolder holder = storageAccess.get(target);
        holder.addArrow(new StoredArrowStack(stored, 1));
    }

    public List<ItemStack> getArrows(LivingEntity target){
        return storageAccess.get(target).getArrows().stream().map(ArrowItemResolver::build).toList();
    }
}
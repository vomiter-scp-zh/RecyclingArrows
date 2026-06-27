package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.world.entity.LivingEntity;

public final class ForgeEntityArrowStorageAccess implements IEntityArrowStorageAccess {
    @Override
    public IArrowRecordHolder get(LivingEntity entity) {
        return entity.getCapability(ModCapabilities.ENTITY_ARROW_STORAGE)
                .orElseGet(() -> {
                    RecyclingArrows.LOGGER.warn(
                            "Missing ENTITY_ARROW_STORAGE capability on entity: {} ({}, {})",
                            entity.getEncodeId(),
                            entity.getUUID(),
                            entity.getClass().getName()
                    );
                    return new ArrowRecordHolder();
                });
    }
}
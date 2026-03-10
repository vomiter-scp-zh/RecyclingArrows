package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.world.entity.LivingEntity;

public final class ForgeEntityArrowStorageAccess implements IEntityArrowStorageAccess {
    @Override
    public IArrowRecordHolder get(LivingEntity entity) {
        return entity.getCapability(ModCapabilities.ENTITY_ARROW_STORAGE)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing ENTITY_ARROW_STORAGE capability on entity: " + entity.getEncodeId()
                ));
    }
}
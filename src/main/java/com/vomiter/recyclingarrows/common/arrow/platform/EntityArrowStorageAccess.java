package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class EntityArrowStorageAccess {
    private static final IEntityArrowStorageAccess IMPL = new ForgeEntityArrowStorageAccess();

    private EntityArrowStorageAccess() {
    }

    public static IArrowRecordHolder getOrThrow(LivingEntity entity) {
        return IMPL.get(entity);
    }

    @Nullable
    public static IArrowRecordHolder getNullable(LivingEntity entity) {
        return entity.getCapability(ModCapabilities.ENTITY_ARROW_STORAGE).resolve().orElse(null);
    }
}
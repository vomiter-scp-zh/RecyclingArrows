package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public final class EntityArrowStorageAccess implements IEntityArrowStorageAccess {
    public EntityArrowStorageAccess() {
    }



    public static IArrowRecordHolder getOrThrow(LivingEntity entity) {
        return getNullable(entity);
    }

    public static @NotNull IArrowRecordHolder getNullable(LivingEntity entity) {
        return entity.getData(RADataAttachments.ARROW_RECORDS);
    }

    @Override
    public IArrowRecordHolder get(LivingEntity entity) {
        return entity.getData(RADataAttachments.ARROW_RECORDS);
    }
}
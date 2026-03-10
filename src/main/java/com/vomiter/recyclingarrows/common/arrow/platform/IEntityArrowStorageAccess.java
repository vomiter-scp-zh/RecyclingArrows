package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.world.entity.LivingEntity;

public interface IEntityArrowStorageAccess {
    IArrowRecordHolder get(LivingEntity entity);
}
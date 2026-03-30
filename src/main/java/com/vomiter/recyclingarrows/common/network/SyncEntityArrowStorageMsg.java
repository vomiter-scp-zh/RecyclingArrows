package com.vomiter.recyclingarrows.common.network;

import net.minecraft.nbt.CompoundTag;

public record SyncEntityArrowStorageMsg(
        int entityId,
        CompoundTag tag
) {}
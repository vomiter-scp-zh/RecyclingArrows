package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolderCodec;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import com.vomiter.recyclingarrows.common.network.SyncEntityArrowStorageMsg;
import com.vomiter.recyclingarrows.common.network.IArrowNetworkBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ArrowSyncService {
    private final IArrowNetworkBridge network;

    public ArrowSyncService(IArrowNetworkBridge network) {
        this.network = network;
    }

    public void sync(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        IArrowRecordHolder holder = EntityArrowStorageAccess.getOrThrow(entity);
        CompoundTag tag = ArrowRecordHolderCodec.save(holder);
        SyncEntityArrowStorageMsg msg = new SyncEntityArrowStorageMsg(entity.getId(), tag);
        network.sendArrowSyncToTracking(entity, msg);
    }

    public void syncToPlayer(LivingEntity entity, ServerPlayer player) {
        if (entity.level().isClientSide) {
            return;
        }

        IArrowRecordHolder holder = EntityArrowStorageAccess.getOrThrow(entity);
        CompoundTag tag = ArrowRecordHolderCodec.save(holder);
        SyncEntityArrowStorageMsg msg = new SyncEntityArrowStorageMsg(entity.getId(), tag);
        network.sendArrowSyncToPlayer(player, msg);
    }
}
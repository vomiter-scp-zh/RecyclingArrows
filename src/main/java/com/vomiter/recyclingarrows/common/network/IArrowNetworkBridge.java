package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.common.network.SyncEntityArrowStorageMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface IArrowNetworkBridge {
    void sendArrowSyncToTracking(LivingEntity entity, SyncEntityArrowStorageMsg msg);
    void sendArrowSyncToPlayer(ServerPlayer player, SyncEntityArrowStorageMsg msg);
}
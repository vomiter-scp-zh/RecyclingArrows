package com.vomiter.recyclingarrows.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NeoForgeArrowNetworkBridge implements IArrowNetworkBridge {

    @Override
    public void sendArrowSyncToTracking(LivingEntity entity, SyncEntityArrowStorageMsg msg) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                entity,
                new SyncEntityArrowStoragePacket(msg)
        );
    }

    @Override
    public void sendArrowSyncToPlayer(ServerPlayer player, SyncEntityArrowStorageMsg msg) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncEntityArrowStoragePacket(msg)
        );
    }
}
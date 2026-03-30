package com.vomiter.recyclingarrows.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

public final class ForgeArrowNetworkBridge implements IArrowNetworkBridge {
    @Override
    public void sendArrowSyncToTracking(LivingEntity entity, SyncEntityArrowStorageMsg msg) {
        ForgeNetworkRegistrar.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new SyncEntityArrowStoragePacket(msg)
        );
    }

    @Override
    public void sendArrowSyncToPlayer(ServerPlayer player, SyncEntityArrowStorageMsg msg) {
        ForgeNetworkRegistrar.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncEntityArrowStoragePacket(msg)
        );
    }
}
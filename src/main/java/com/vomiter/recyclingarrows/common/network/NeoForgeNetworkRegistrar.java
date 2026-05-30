package com.vomiter.recyclingarrows.common.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNetworkRegistrar {

    private NeoForgeNetworkRegistrar() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncEntityArrowStoragePacket.TYPE,
                SyncEntityArrowStoragePacket.STREAM_CODEC,
                NeoForgeNetworkRegistrar::handleSyncEntityArrowStorage
        );
    }

    private static void handleSyncEntityArrowStorage(
            SyncEntityArrowStoragePacket packet,
            net.neoforged.neoforge.network.handling.IPayloadContext context
    ) {
        SyncEntityArrowStorageHandler.handleClient(packet.msg());
    }
}
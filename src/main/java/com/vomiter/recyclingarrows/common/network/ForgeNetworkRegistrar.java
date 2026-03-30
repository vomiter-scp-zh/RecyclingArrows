package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetworkRegistrar {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RecyclingArrows.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ForgeNetworkRegistrar() {
    }

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(
                id++,
                SyncEntityArrowStoragePacket.class,
                SyncEntityArrowStoragePacket::encode,
                SyncEntityArrowStoragePacket::decode,
                SyncEntityArrowStoragePacket::handle
        );
    }
}
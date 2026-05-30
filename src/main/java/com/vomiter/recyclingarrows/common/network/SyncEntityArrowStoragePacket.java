package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncEntityArrowStoragePacket(SyncEntityArrowStorageMsg msg) implements CustomPacketPayload {

    public static final Type<SyncEntityArrowStoragePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RecyclingArrows.MOD_ID, "sync_entity_arrow_storage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityArrowStoragePacket> STREAM_CODEC =
            StreamCodec.of(
                    SyncEntityArrowStoragePacket::encode,
                    SyncEntityArrowStoragePacket::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, SyncEntityArrowStoragePacket packet) {
        SyncEntityArrowStorageMsgCodec.encode(buf, packet.msg);
    }

    private static SyncEntityArrowStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncEntityArrowStoragePacket(SyncEntityArrowStorageMsgCodec.decode(buf));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
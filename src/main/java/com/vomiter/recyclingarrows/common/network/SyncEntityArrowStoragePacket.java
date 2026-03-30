package com.vomiter.recyclingarrows.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SyncEntityArrowStoragePacket {
    private final SyncEntityArrowStorageMsg msg;

    public SyncEntityArrowStoragePacket(SyncEntityArrowStorageMsg msg) {
        this.msg = msg;
    }

    public SyncEntityArrowStorageMsg msg() {
        return msg;
    }

    public static void encode(SyncEntityArrowStoragePacket packet, FriendlyByteBuf buf) {
        SyncEntityArrowStorageMsgCodec.encode(buf, packet.msg);
    }

    public static SyncEntityArrowStoragePacket decode(FriendlyByteBuf buf) {
        return new SyncEntityArrowStoragePacket(SyncEntityArrowStorageMsgCodec.decode(buf));
    }

    public static void handle(SyncEntityArrowStoragePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        SyncEntityArrowStorageHandler.handleClient(packet.msg)
                )
        );
        ctx.setPacketHandled(true);
    }
}
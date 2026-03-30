package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.common.network.SyncEntityArrowStorageMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class SyncEntityArrowStorageMsgCodec {
    private SyncEntityArrowStorageMsgCodec() {
    }

    public static void encode(FriendlyByteBuf buf, SyncEntityArrowStorageMsg msg) {
        buf.writeVarInt(msg.entityId());
        buf.writeNbt(msg.tag());
    }

    public static SyncEntityArrowStorageMsg decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        CompoundTag tag = buf.readNbt();
        if (tag == null) {
            tag = new CompoundTag();
        }
        return new SyncEntityArrowStorageMsg(entityId, tag);
    }
}
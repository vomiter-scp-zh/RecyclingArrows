package com.vomiter.recyclingarrows.common.network;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolderCodec;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import com.vomiter.recyclingarrows.common.network.SyncEntityArrowStorageMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class SyncEntityArrowStorageHandler {
    private SyncEntityArrowStorageHandler() {
    }

    public static void handleClient(SyncEntityArrowStorageMsg msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Entity entity = mc.level.getEntity(msg.entityId());
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        IArrowRecordHolder holder = EntityArrowStorageAccess.getNullable(living);
        if (holder == null) {
            return;
        }

        holder.clear();
        ArrowRecordHolderCodec.load(holder, msg.tag());
    }
}
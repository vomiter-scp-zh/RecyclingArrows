package com.vomiter.recyclingarrows.common.arrow.sync;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID)
public final class ArrowTrackingEvents {
    private ArrowTrackingEvents() {
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        RecyclingArrows.arrowSyncService.syncToPlayer(living, serverPlayer);
    }
}
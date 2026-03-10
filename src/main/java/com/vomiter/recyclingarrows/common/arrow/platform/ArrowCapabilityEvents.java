package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID)
public final class ArrowCapabilityEvents {
    private static final ResourceLocation ARROW_STORAGE_ID =
            RecyclingArrows.modLoc( "entity_arrow_storage");

    private ArrowCapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(ARROW_STORAGE_ID, new ArrowStorageProvider());
        }
    }
}
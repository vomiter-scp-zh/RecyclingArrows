package com.vomiter.recyclingarrows.client;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRenderEvents {
    private ClientRenderEvents() {
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        for (String skin : event.getSkins()) {
            LivingEntityRenderer<?, ?> playerRenderer = event.getSkin(skin);
            if (playerRenderer != null) {
                addLayerUnchecked(playerRenderer, dispatcher);
            }
        }

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            tryAddLayerToEntityRenderer(event, type, dispatcher);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void tryAddLayerToEntityRenderer(EntityRenderersEvent.AddLayers event,
                                                    EntityType<?> rawType,
                                                    EntityRenderDispatcher dispatcher) {
        EntityRenderer renderer = event.getEntityRenderer(rawType);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new RecyclingArrowLayer<>(livingRenderer, dispatcher));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerUnchecked(LivingEntityRenderer<?, ?> renderer,
                                          EntityRenderDispatcher dispatcher) {
        ((LivingEntityRenderer) renderer).addLayer(new RecyclingArrowLayer(renderer, dispatcher));
    }
}
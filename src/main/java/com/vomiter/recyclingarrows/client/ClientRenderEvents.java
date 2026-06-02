package com.vomiter.recyclingarrows.client;

import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = RecyclingArrows.MOD_ID)
public final class ClientRenderEvents {
    private ClientRenderEvents() {
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> playerRenderer = event.getSkin(skin);
            if (playerRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                addLayerUnchecked(livingRenderer, dispatcher);
            }
        }

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            tryAddLayerToEntityRenderer(event, type, dispatcher);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void tryAddLayerToEntityRenderer(EntityRenderersEvent.AddLayers event,
                                                    EntityType<?> type,
                                                    EntityRenderDispatcher dispatcher) {
        EntityRenderer<?> renderer = event.getRenderer(type);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new RecyclingArrowLayer<>(livingRenderer, dispatcher));
        }
        else if(ModList.get().isLoaded("geckolib")) {
            GeckoLibCompat.addLayer(renderer, dispatcher);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerUnchecked(LivingEntityRenderer<?, ?> renderer,
                                          EntityRenderDispatcher dispatcher) {
        renderer.addLayer(new RecyclingArrowLayer(renderer, dispatcher));
    }
}
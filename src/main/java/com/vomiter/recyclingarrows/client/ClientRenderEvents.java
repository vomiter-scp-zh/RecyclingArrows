package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.platform.Window;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public final class ClientRenderEvents {

    public static void init(IEventBus modBus){
        modBus.addListener(ClientRenderEvents::onAddLayers);
        NeoForge.EVENT_BUS.addListener(ForgeEvents::onRenderGameOverlayPost);
    }

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

    public static final class ForgeEvents {

        public static void onRenderGameOverlayPost(RenderGuiLayerEvent.Post event){
            final Minecraft minecraft = Minecraft.getInstance();
            final Player player = minecraft.player;
            final Window window = minecraft.getWindow();
            if (player != null)
            {
                if (
                    event.getName() == VanillaGuiLayers.CROSSHAIR
                    && minecraft.screen == null
                    && (! player.isShiftKeyDown())
                ) {
                    HitResult hitResult = minecraft.hitResult;

                    if (hitResult instanceof EntityHitResult entityHitResult
                            && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                        var holder = EntityArrowStorageAccess.getNullable(livingEntity);
                        if (holder == null) return;

                        List<StoredArrowStack> stacks = holder.getArrows();
                        if (stacks.isEmpty()) return;
                        for (int i = 0; i < stacks.size(); i++) {
                            var count = stacks.get(i).getCount();
                            Item item = BuiltInRegistries.ITEM.get(stacks.get(i).getArrow().itemId());
                            if (item == null) continue;
                            int x = window.getGuiScaledWidth() / 2 + 3;
                            int y = window.getGuiScaledHeight() / 2 - 16 + 24 * i;
                            ArrowScreenOverlay.drawArrowIndicator(
                                    minecraft,
                                    event.getGuiGraphics(),
                                    item.getDefaultInstance(),
                                    count,
                                    x, y
                            );
                        }

                    }
                }
            }
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

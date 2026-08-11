package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.platform.Window;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientRenderEvents {
    private static HashMap<EntityType<?>, Boolean> IS_LIVING = new HashMap<>();
    private static Boolean getIsLiving(EntityType<?> type){
        return IS_LIVING.computeIfAbsent(type, t -> {
            assert Minecraft.getInstance().level != null;
            return (t.create(Minecraft.getInstance().level) instanceof LivingEntity);
        });
    }
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

    @Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {

        @SubscribeEvent
        public static void onRenderGameOverlayPost(RenderGuiOverlayEvent.Post event){
            final Minecraft minecraft = Minecraft.getInstance();
            final Player player = minecraft.player;
            final Window window = minecraft.getWindow();
            if (player != null)
            {
                if (
                    event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()
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
                            Item item = ForgeRegistries.ITEMS.getValue(stacks.get(i).getArrow().itemId());
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
                                                    EntityType<?> rawType,
                                                    EntityRenderDispatcher dispatcher) {
        EntityRenderer renderer = event.getEntityRenderer(rawType);

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

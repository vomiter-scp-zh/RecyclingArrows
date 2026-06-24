package com.vomiter.recyclingarrows.common.event;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowDropDataManager;
import com.vomiter.recyclingarrows.common.command.ModCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShearsItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventHandler {
    public static void init() {
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventHandler::onRegisterCommands);
        bus.addListener(EventHandler::onAddReloadListener);
        bus.addListener(EventHandler::onLivingTick);
        bus.addListener(EventHandler::onInteract);
        bus.addListener(EventHandler::onRightClickAir);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(ArrowDropDataManager.INSTANCE);
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event){
        var living = event.getEntity();
        if(living.tickCount % 500 != 0) return;
        if(!living.getRandom().nextBoolean()) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(living);
        if(storedArrow == null) return;
        if(living instanceof Player player){
            player.displayClientMessage(Component.translatable("msg.recyclingarrows.arrow_drop"), true);
        }
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, living.getRandom());
        items.forEach(living::spawnAtLocation);
    }

    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event){
        Player player = event.getEntity();
        if(player.isCrouching() && player.getMainHandItem().getItem() instanceof ShearsItem shears){
            var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(player);
            if(storedArrow == null) return;
            player.hurt(player.damageSources().playerAttack(event.getEntity()), 0.5f);
            event.getEntity().getItemInHand(event.getHand()).hurtAndBreak(1, event.getEntity(), p -> p.broadcastBreakEvent(event.getHand()));
            player.displayClientMessage(Component.translatable("msg.recyclingarrows.arrow_remove"), true);
            var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, player.getRandom());
            items.forEach(player::spawnAtLocation);
            event.setCanceled(true);
        }
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event){
        if(!(event.getTarget() instanceof LivingEntity living)) return;
        if(!(event.getEntity().getItemInHand(event.getHand()).getItem() instanceof ShearsItem shears)) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(living);
        if(storedArrow == null) return;
        living.hurt(living.damageSources().playerAttack(event.getEntity()), 0.5f);
        event.getEntity().getItemInHand(event.getHand()).hurtAndBreak(1, event.getEntity(), p -> p.broadcastBreakEvent(event.getHand()));
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, living.getRandom());
        items.forEach(living::spawnAtLocation);
        event.setCanceled(true);
    }
}
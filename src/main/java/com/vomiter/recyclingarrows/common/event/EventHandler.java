package com.vomiter.recyclingarrows.common.event;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowDropDataManager;
import com.vomiter.recyclingarrows.common.arrow.logic.ArrowItemResolver;
import com.vomiter.recyclingarrows.common.command.ModCommand;
import net.minecraft.world.entity.Mob;
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
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(ArrowDropDataManager.INSTANCE);
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event){
        if(!(event.getEntity() instanceof Mob mob)) return;
        if(mob.tickCount % 500 != 0) return;
        if(!mob.getRandom().nextBoolean()) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(mob);
        if(storedArrow == null) return;
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, mob.getRandom());
        items.forEach(mob::spawnAtLocation);
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event){
        if(!(event.getTarget() instanceof Mob mob)) return;
        if(!(event.getEntity().getItemInHand(event.getHand()).getItem() instanceof ShearsItem shears)) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(mob);
        if(storedArrow == null) return;
        mob.hurt(mob.damageSources().playerAttack(event.getEntity()), 0.5f);
        event.getEntity().getItemInHand(event.getHand()).hurtAndBreak(1, event.getEntity(), living -> living.broadcastBreakEvent(event.getHand()));
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, mob.getRandom());
        items.forEach(mob::spawnAtLocation);
        event.setCanceled(true);
    }
}
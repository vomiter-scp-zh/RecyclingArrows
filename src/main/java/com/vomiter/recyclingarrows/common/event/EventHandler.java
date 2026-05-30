package com.vomiter.recyclingarrows.common.event;

import com.vomiter.recyclingarrows.Helpers;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowDropDataManager;
import com.vomiter.recyclingarrows.common.command.ModCommand;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ShearsItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Objects;

public class EventHandler {
    public static void init() {
        final IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(EventHandler::onRegisterCommands);
        bus.addListener(EventHandler::onAddReloadListener);
        bus.addListener(EventHandler::onLivingTick);
        bus.addListener(EventHandler::onInteract);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(RecyclingArrows.MOD_ID, RecyclingArrows.MOD_ID) ,ArrowDropDataManager.INSTANCE);
    }

    public static void onLivingTick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob)) return;
        if(mob.tickCount % 500 != 0) return;
        if(!mob.getRandom().nextBoolean()) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(mob);
        if(storedArrow == null) return;
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, mob.getRandom());
        items.forEach(itemStack -> {
            if(mob.level() instanceof ServerLevel serverLevel) mob.spawnAtLocation(serverLevel, itemStack);
        });
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event){
        if(!(event.getTarget() instanceof Mob mob)) return;
        if(!(event.getEntity().getItemInHand(event.getHand()).getItem() instanceof ShearsItem shears)) return;
        var storedArrow = RecyclingArrows.ARROW_HIT_SERVICE.removeArrow(mob);
        if(storedArrow == null) return;
        mob.hurt(mob.damageSources().playerAttack(event.getEntity()), 0.5f);
        event.getEntity().getItemInHand(event.getHand()).hurtAndBreak(1, event.getEntity(), Objects.requireNonNull(Helpers.getHandSlot(event.getHand())));
        var items = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, mob.getRandom());
        items.forEach(itemStack -> {
            if(mob.level() instanceof ServerLevel serverLevel) mob.spawnAtLocation(serverLevel, itemStack);
        });
        event.setCanceled(true);
    }
}
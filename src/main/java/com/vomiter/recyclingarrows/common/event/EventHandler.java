package com.vomiter.recyclingarrows.common.event;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowDropDataManager;
import com.vomiter.recyclingarrows.common.command.ModCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventHandler {
    public static void init() {
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventHandler::onRegisterCommands);
        bus.addListener(EventHandler::onAddReloadListener);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(ArrowDropDataManager.INSTANCE);
    }
}
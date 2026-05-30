package com.vomiter.recyclingarrows;

import com.mojang.logging.LogUtils;
import com.vomiter.recyclingarrows.common.arrow.logic.ArrowHitService;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import com.vomiter.recyclingarrows.common.arrow.platform.RADataAttachments;
import com.vomiter.recyclingarrows.common.event.EventHandler;
import com.vomiter.recyclingarrows.common.network.ArrowSyncService;
import com.vomiter.recyclingarrows.common.network.NeoForgeArrowNetworkBridge;
import com.vomiter.recyclingarrows.common.network.NeoForgeNetworkRegistrar;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(RecyclingArrows.MOD_ID)
public class RecyclingArrows
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "recyclingarrows";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ArrowSyncService arrowSyncService;

    public static ResourceLocation modLoc(String path){
        return Helpers.id(RecyclingArrows.MOD_ID, path);
    }

    public static final ArrowHitService ARROW_HIT_SERVICE =
            new ArrowHitService(new EntityArrowStorageAccess());


    public RecyclingArrows(ModContainer mod, IEventBus modBus) {
        EventHandler.init();
        modBus.addListener(this::commonSetup);
        mod.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        RADataAttachments.register(modBus);
        modBus.addListener(NeoForgeNetworkRegistrar::registerPayloads);
        arrowSyncService = new ArrowSyncService(new NeoForgeArrowNetworkBridge());

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

}

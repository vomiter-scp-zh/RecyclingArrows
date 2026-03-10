package com.vomiter.recyclingarrows;

import com.mojang.logging.LogUtils;
import com.vomiter.recyclingarrows.common.arrow.logic.ArrowHitService;
import com.vomiter.recyclingarrows.common.arrow.platform.ForgeEntityArrowStorageAccess;
import com.vomiter.recyclingarrows.common.event.EventHandler;
import com.vomiter.recyclingarrows.common.registry.ModRegistries;
import com.vomiter.recyclingarrows.data.ModDataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RecyclingArrows.MOD_ID)
public class RecyclingArrows
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "recyclingarrows";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation modLoc(String path){
        return Helpers.id(RecyclingArrows.MOD_ID, path);
    }

    public static final ArrowHitService ARROW_HIT_SERVICE =
            new ArrowHitService(new ForgeEntityArrowStorageAccess());


    public RecyclingArrows(FMLJavaModLoadingContext context) {
        EventHandler.init();
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(ModDataGenerator::generateData);
        ModRegistries.register(modBus);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

}

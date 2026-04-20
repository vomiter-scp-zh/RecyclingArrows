package com.vomiter.recyclingarrows;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_DATAPACK_DROPS = BUILDER
            .comment("If false, disable recycling_arrows datapack drop override system and always fallback to original resolved arrow item.")
            .define("enableDatapackDrops", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    public static boolean enableDatapackDrops() {
        return ENABLE_DATAPACK_DROPS.get();
    }
}
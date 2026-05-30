package com.vomiter.recyclingarrows;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = RecyclingArrows.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DATAPACK_DROPS = BUILDER
            .comment("If false, disable recycling_arrows datapack drop override system and always fallback to original resolved arrow item.")
            .define("enableDatapackDrops", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    public static boolean enableDatapackDrops() {
        return ENABLE_DATAPACK_DROPS.get();
    }
}
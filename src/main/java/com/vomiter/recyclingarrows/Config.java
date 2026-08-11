package com.vomiter.recyclingarrows;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = RecyclingArrows.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_DATAPACK_DROPS = BUILDER
            .comment("If false, disable recycling_arrows datapack drop override system and always fallback to original resolved arrow item.")
            .define("enableDatapackDrops", true);

    public static final ForgeConfigSpec.BooleanValue DISABLE_UNPICKABLE_ARROW_RECYCLING = BUILDER
            .comment("If true, arrows shot by monsters or bows enchanted with infinity won't be recorded.")
            .define("disableUnpickableArrowRecycling", true);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    /**
     * Entity types whose embedded arrows are represented by a compact floating indicator instead.
     * Each entry must be a namespaced entity type id, such as "minecraft:ender_dragon".
     */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INDICATOR_ENTITY_TYPES = CLIENT_BUILDER
            .comment("Entity type ids that render embedded arrows as a floating item/count indicator instead of physical arrows.")
            .defineListAllowEmpty("indicatorEntityTypes", List::of, value -> value instanceof String);

    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    public static boolean enableDatapackDrops() {
        return ENABLE_DATAPACK_DROPS.get();
    }
    public static boolean disableUnpickableArrowRecycling() {
        return DISABLE_UNPICKABLE_ARROW_RECYCLING.get();
    }


    public static List<? extends String> indicatorEntityTypes() {
        return INDICATOR_ENTITY_TYPES.get();
    }
}

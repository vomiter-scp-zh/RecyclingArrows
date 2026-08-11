package com.vomiter.recyclingarrows;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = RecyclingArrows.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DATAPACK_DROPS = BUILDER
            .comment("If false, disable recycling_arrows datapack drop override system and always fallback to original resolved arrow item.")
            .define("enableDatapackDrops", true);

    public static final ModConfigSpec.BooleanValue DISABLE_UNPICKABLE_ARROW_RECYCLING = BUILDER
            .comment("If true, arrows shot by monsters or bows enchanted with infinity won't be recorded.")
            .define("disableUnpickableArrowRecycling", true);
    static final ModConfigSpec SPEC = BUILDER.build();

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    /**
     * Entity types whose embedded arrows are represented by a compact floating indicator instead.
     * Each entry must be a namespaced entity type id, such as "minecraft:ender_dragon".
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> INDICATOR_ENTITY_TYPES = CLIENT_BUILDER
            .comment("Entity type ids that render embedded arrows as a floating item/count indicator instead of physical arrows.")
            .defineListAllowEmpty("indicatorEntityTypes", List::of, value -> value instanceof String);

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

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

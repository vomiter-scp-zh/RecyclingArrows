package com.vomiter.recyclingarrows.common.arrow.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vomiter.recyclingarrows.Config;
import com.vomiter.recyclingarrows.RecyclingArrows;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ArrowDropDataManager
        extends SimpleJsonResourceReloadListener<ArrowDropDataManager.@NotNull ArrowDropDefinition> {

    private static final String DIRECTORY = "recycling_arrows";
    private static final String FUNCTION_COPY_DATA = "copy_data";

    public static final Identifier RELOAD_LISTENER_ID =
            Identifier.fromNamespaceAndPath(RecyclingArrows.MOD_ID, "arrow_drop_data");

    public static final ArrowDropDataManager INSTANCE = new ArrowDropDataManager();

    private Map<Identifier, ArrowDropDefinition> definitions = Map.of();

    private ArrowDropDataManager() {
        super(
                ArrowDropDefinition.CODEC,
                FileToIdConverter.json(DIRECTORY)
        );
    }

    @Override
    protected void apply(
            Map<Identifier, ArrowDropDefinition> objectMap,
            @NotNull ResourceManager resourceManager,
            @NotNull ProfilerFiller profiler
    ) {
        this.definitions = Map.copyOf(objectMap);

        RecyclingArrows.LOGGER.info(
                "Loaded {} recycling_arrows definitions",
                this.definitions.size()
        );
    }

    public List<ItemStack> resolveDrops(StoredArrow storedArrow, RandomSource random) {
        if (storedArrow == null || storedArrow.isEmpty()) {
            return List.of();
        }

        if (!Config.enableDatapackDrops()) {
            return fallbackOriginal(storedArrow);
        }

        if (random == null) {
            random = RandomSource.create();
        }

        return resolveDropsInternal(storedArrow, random, new HashSet<>());
    }

    private List<ItemStack> resolveDropsInternal(
            StoredArrow storedArrow,
            RandomSource random,
            Set<Identifier> visiting
    ) {
        Identifier arrowId = storedArrow.itemId();
        ArrowDropDefinition definition = definitions.get(arrowId);

        if (definition == null) {
            return fallbackOriginal(storedArrow);
        }

        if (!visiting.add(arrowId)) {
            RecyclingArrows.LOGGER.warn(
                    "Detected cyclic recycling_arrows reference at {}, fallback to original result",
                    arrowId
            );
            return fallbackOriginal(storedArrow);
        }

        try {
            ArrowDropPool chosen = pickWeighted(definition.pools(), random);
            if (chosen == null) {
                return List.of();
            }

            if (chosen.reference() != null) {
                StoredArrow referenced = storedArrow.copyAs(chosen.reference());
                return resolveDropsInternal(referenced, random, visiting);
            }

            List<ItemStack> result = new ArrayList<>();

            for (ArrowDropEntry entry : chosen.entries()) {
                if (entry.chance() < 1.0F && random.nextFloat() > entry.chance()) {
                    continue;
                }

                ItemStack built = buildEntry(entry, storedArrow);
                if (!built.isEmpty()) {
                    result.add(built);
                }
            }

            return result;
        } finally {
            visiting.remove(arrowId);
        }
    }

    private static ItemStack buildEntry(ArrowDropEntry entry, StoredArrow sourceArrow) {
        Item item = BuiltInRegistries.ITEM.getValue(entry.itemId());

        if (item == null || item == Items.AIR) {
            RecyclingArrows.LOGGER.warn(
                    "Unknown item id in recycling_arrows drop entry: {}",
                    entry.itemId()
            );
            return ItemStack.EMPTY;
        }

        if (entry.copyData()) {
            return sourceArrow.copyAsStack(entry.itemId());
        }

        return new ItemStack(item);
    }

    private static List<ItemStack> fallbackOriginal(StoredArrow storedArrow) {
        ItemStack original = storedArrow.stack().copy();
        if (original.isEmpty()) {
            return List.of();
        }

        return List.of(original);
    }

    private static ArrowDropPool pickWeighted(List<ArrowDropPool> pools, RandomSource random) {
        if (pools.isEmpty()) {
            return null;
        }

        int totalWeight = 0;

        for (ArrowDropPool pool : pools) {
            if (pool.weight() > 0) {
                totalWeight += pool.weight();
            }
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int cursor = 0;

        for (ArrowDropPool pool : pools) {
            if (pool.weight() <= 0) {
                continue;
            }

            cursor += pool.weight();

            if (roll < cursor) {
                return pool;
            }
        }

        return pools.getLast();
    }

    public boolean hasDefinition(Identifier arrowId) {
        return definitions.containsKey(arrowId);
    }

    public Map<Identifier, ArrowDropDefinition> getDefinitionsView() {
        return Collections.unmodifiableMap(definitions);
    }

    public record ArrowDropDefinition(List<ArrowDropPool> pools) {
        public static final Codec<ArrowDropDefinition> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ArrowDropPool.CODEC
                                .listOf()
                                .fieldOf("results")
                                .forGetter(ArrowDropDefinition::pools)
                ).apply(instance, ArrowDropDefinition::new)
        );
    }

    public record ArrowDropPool(int weight, List<ArrowDropEntry> entries, Identifier reference) {
        private static final Codec<ArrowDropPoolRaw> RAW_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT
                                .optionalFieldOf("weight", 1)
                                .forGetter(ArrowDropPoolRaw::weight),

                        ArrowDropEntry.CODEC
                                .listOf()
                                .optionalFieldOf("items", List.of())
                                .forGetter(ArrowDropPoolRaw::items),

                        Identifier.CODEC
                                .optionalFieldOf("result")
                                .forGetter(ArrowDropPoolRaw::result),

                        Identifier.CODEC
                                .optionalFieldOf("results")
                                .forGetter(ArrowDropPoolRaw::results)
                ).apply(instance, ArrowDropPoolRaw::new)
        );

        public static final Codec<ArrowDropPool> CODEC =
                RAW_CODEC.comapFlatMap(ArrowDropPool::fromRaw, ArrowDropPool::toRaw);

        private static DataResult<ArrowDropPool> fromRaw(ArrowDropPoolRaw raw) {
            boolean hasItems = !raw.items().isEmpty();
            boolean hasResult = raw.result().isPresent();
            boolean hasResults = raw.results().isPresent();

            int referenceCount = (hasResult ? 1 : 0) + (hasResults ? 1 : 0);

            if (hasItems && referenceCount > 0) {
                return DataResult.error(() ->
                        "A recycling_arrows result pool cannot contain both 'items' and 'result'/'results'"
                );
            }

            if (!hasItems && referenceCount == 0) {
                return DataResult.error(() ->
                        "A recycling_arrows result pool must contain either 'items' or string 'result'/'results'"
                );
            }

            if (referenceCount > 1) {
                return DataResult.error(() ->
                        "A recycling_arrows result pool cannot contain both 'result' and 'results'"
                );
            }

            Identifier reference = raw.result().or(() -> raw.results()).orElse(null);

            return DataResult.success(
                    new ArrowDropPool(
                            raw.weight(),
                            List.copyOf(raw.items()),
                            reference
                    )
            );
        }

        private ArrowDropPoolRaw toRaw() {
            return new ArrowDropPoolRaw(
                    weight,
                    entries,
                    Optional.ofNullable(reference),
                    Optional.empty()
            );
        }
    }

    private record ArrowDropPoolRaw(
            int weight,
            List<ArrowDropEntry> items,
            Optional<Identifier> result,
            Optional<Identifier> results
    ) {
    }

    public record ArrowDropEntry(Identifier itemId, float chance, boolean copyData) {
        private static final Codec<ArrowDropEntry> STRING_CODEC =
                Identifier.CODEC.xmap(
                        id -> new ArrowDropEntry(id, 1.0F, false),
                        ArrowDropEntry::itemId
                );

        private static final Codec<ArrowDropEntry> OBJECT_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC
                                .fieldOf("item")
                                .forGetter(ArrowDropEntry::itemId),

                        Codec.FLOAT
                                .optionalFieldOf("chance", 1.0F)
                                .forGetter(ArrowDropEntry::chance),

                        Codec.STRING
                                .optionalFieldOf("function", "")
                                .xmap(
                                        function -> {
                                            if (function.isEmpty()) {
                                                return false;
                                            }

                                            if (FUNCTION_COPY_DATA.equals(function)) {
                                                return true;
                                            }

                                            throw new IllegalArgumentException("Unknown function: " + function);
                                        },
                                        copyData -> copyData ? FUNCTION_COPY_DATA : ""
                                )
                                .forGetter(ArrowDropEntry::copyData)
                ).apply(instance, ArrowDropEntry::new)
        );

        public static final Codec<ArrowDropEntry> CODEC =
                Codec.either(STRING_CODEC, OBJECT_CODEC).xmap(
                        either -> either.map(left -> left, right -> right),
                        entry -> entry.chance() == 1.0F && !entry.copyData()
                                ? com.mojang.datafixers.util.Either.left(entry)
                                : com.mojang.datafixers.util.Either.right(entry)
                );
    }
}
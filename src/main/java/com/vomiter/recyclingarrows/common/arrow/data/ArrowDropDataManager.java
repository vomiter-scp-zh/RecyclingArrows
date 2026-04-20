package com.vomiter.recyclingarrows.common.arrow.data;

import com.google.gson.*;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.logic.ArrowItemResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ArrowDropDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String DIRECTORY = "recycling_arrows";
    private static final String FUNCTION_COPY_DATA = "copy_data";

    private Map<ResourceLocation, ArrowDropDefinition> definitions = Map.of();

    private ArrowDropDataManager() {
        super(GSON, DIRECTORY);
    }

    public static final ArrowDropDataManager INSTANCE = new ArrowDropDataManager();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, ArrowDropDefinition> parsed = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objectMap.entrySet()) {
            ResourceLocation arrowId = entry.getKey();

            try {
                if (!entry.getValue().isJsonObject()) {
                    RecyclingArrows.LOGGER.warn("Skipping recycling_arrows entry {} because root is not an object", arrowId);
                    continue;
                }

                ArrowDropDefinition definition = parseDefinition(arrowId, entry.getValue().getAsJsonObject());
                parsed.put(arrowId, definition);
            } catch (Exception e) {
                RecyclingArrows.LOGGER.error("Failed to parse recycling_arrows data for {}", arrowId, e);
            }
        }

        this.definitions = Map.copyOf(parsed);
        RecyclingArrows.LOGGER.info("Loaded {} recycling_arrows definitions", this.definitions.size());
    }

    /**
     * 對外主入口：
     * - 若有對應 datapack，就依 weight 抽一個 pool，再產生掉落物
     * - 若沒有對應 datapack，就 fallback 成原本箭矢解析結果
     */
    public List<ItemStack> resolveDrops(StoredArrow storedArrow, RandomSource random) {
        if (storedArrow == null) {
            return List.of();
        }

        if (random == null) {
            random = RandomSource.create();
        }

        return resolveDropsInternal(storedArrow, random, new HashSet<>());
    }

    private List<ItemStack> resolveDropsInternal(StoredArrow storedArrow, RandomSource random, Set<ResourceLocation> visiting) {
        ResourceLocation arrowId = storedArrow.itemId();
        ArrowDropDefinition definition = definitions.get(arrowId);

        // 沒有任何 datapack 定義 -> fallback 原本解析結果
        if (definition == null) {
            return fallbackOriginal(storedArrow);
        }

        // 防止 A -> B -> A 這種循環參照
        if (!visiting.add(arrowId)) {
            RecyclingArrows.LOGGER.warn("Detected cyclic recycling_arrows reference at {}, fallback to original result", arrowId);
            return fallbackOriginal(storedArrow);
        }

        try {
            ArrowDropPool chosen = pickWeighted(definition.pools(), random);
            if (chosen == null) {
                return List.of();
            }

            // 參照另一個箭種的結果
            if (chosen.reference() != null) {
                StoredArrow referenced = new StoredArrow(chosen.reference(), copyTagOrEmpty(storedArrow.tag()));
                return resolveDropsInternal(referenced, random, visiting);
            }

            // items additive 掉落
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
        Item item = ForgeRegistries.ITEMS.getValue(entry.itemId());
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item);

        if (entry.copyData()) {
            CompoundTag sourceTag = sourceArrow.tag();
            if (sourceTag != null && !sourceTag.isEmpty()) {
                stack.setTag(sourceTag.copy());
            }
        }

        return stack;
    }

    private static List<ItemStack> fallbackOriginal(StoredArrow storedArrow) {
        ItemStack original = ArrowItemResolver.build(storedArrow);
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

        return pools.get(pools.size() - 1);
    }

    private static ArrowDropDefinition parseDefinition(ResourceLocation arrowId, JsonObject root) {
        JsonArray resultsArray = GsonHelper.getAsJsonArray(root, "results");
        List<ArrowDropPool> pools = new ArrayList<>();

        for (JsonElement element : resultsArray) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Each result pool must be an object: " + arrowId);
            }

            JsonObject poolObj = element.getAsJsonObject();
            int weight = GsonHelper.getAsInt(poolObj, "weight", 1);

            // 支援：
            // 1) "items": [...]
            // 2) "results": "minecraft:arrow"  (你範例使用這個)
            // 3) "result": "minecraft:arrow"   (順手容錯)
            ResourceLocation reference = null;
            List<ArrowDropEntry> entries = List.of();

            if (poolObj.has("items")) {
                entries = parseItemsArray(GsonHelper.getAsJsonArray(poolObj, "items"));
            } else if (poolObj.has("results") && poolObj.get("results").isJsonPrimitive()) {
                reference = parseResourceLocation(poolObj.get("results").getAsString(), "results reference", arrowId);
            } else if (poolObj.has("result") && poolObj.get("result").isJsonPrimitive()) {
                reference = parseResourceLocation(poolObj.get("result").getAsString(), "result reference", arrowId);
            } else {
                throw new IllegalArgumentException("Pool must contain either 'items' array or string 'results'/'result' reference: " + arrowId);
            }

            pools.add(new ArrowDropPool(weight, entries, reference));
        }

        return new ArrowDropDefinition(List.copyOf(pools));
    }

    private static List<ArrowDropEntry> parseItemsArray(JsonArray itemsArray) {
        List<ArrowDropEntry> entries = new ArrayList<>();

        for (JsonElement itemElement : itemsArray) {
            if (itemElement.isJsonPrimitive() && itemElement.getAsJsonPrimitive().isString()) {
                ResourceLocation itemId = parseResourceLocation(itemElement.getAsString(), "item", null);
                entries.add(new ArrowDropEntry(itemId, 1.0F, false));
                continue;
            }

            if (!itemElement.isJsonObject()) {
                throw new IllegalArgumentException("Each item entry must be a string or object");
            }

            JsonObject itemObj = itemElement.getAsJsonObject();
            ResourceLocation itemId = parseResourceLocation(GsonHelper.getAsString(itemObj, "item"), "item", null);
            float chance = GsonHelper.getAsFloat(itemObj, "chance", 1.0F);
            boolean copyData = false;

            if (itemObj.has("function")) {
                String function = GsonHelper.getAsString(itemObj, "function");
                if (FUNCTION_COPY_DATA.equals(function)) {
                    copyData = true;
                } else {
                    throw new IllegalArgumentException("Unknown function: " + function);
                }
            }

            entries.add(new ArrowDropEntry(itemId, chance, copyData));
        }

        return List.copyOf(entries);
    }

    private static ResourceLocation parseResourceLocation(String raw, String fieldName, ResourceLocation context) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            throw new IllegalArgumentException("Invalid ResourceLocation in field '" + fieldName + "': " + raw
                    + (context != null ? " (from " + context + ")" : ""));
        }
        return id;
    }

    private static CompoundTag copyTagOrEmpty(CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag.copy();
    }

    public boolean hasDefinition(ResourceLocation arrowId) {
        return definitions.containsKey(arrowId);
    }

    public Map<ResourceLocation, ArrowDropDefinition> getDefinitionsView() {
        return Collections.unmodifiableMap(definitions);
    }

    public record ArrowDropDefinition(List<ArrowDropPool> pools) {
    }

    public record ArrowDropPool(int weight, List<ArrowDropEntry> entries, ResourceLocation reference) {
    }

    public record ArrowDropEntry(ResourceLocation itemId, float chance, boolean copyData) {
    }
}
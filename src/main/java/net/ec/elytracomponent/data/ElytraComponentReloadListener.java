package net.ec.elytracomponent.data;

import com.google.gson.*;
import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据包资源重载监听器，读取 data/<namespace>/elytra_components.json 文件。
 * 允许数据包/其他模组通过 JSON 注册鞘翅组件定义。
 */
public class ElytraComponentReloadListener extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger(ElytraComponentReloadListener.class);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    /** 所有已注册的组件定义：componentId → Definition */
    private static final Map<String, ElytraComponentDefinition> REGISTRY = new HashMap<>();

    /** 代码注册的组件定义（优先级高于 JSON） */
    private static final Map<String, ElytraComponentDefinition> CODE_REGISTRY = new HashMap<>();

    /** 物品ID → 组件定义的反向索引（用于快速查找） */
    private static Map<ResourceLocation, ElytraComponentDefinition> ITEM_INDEX = Collections.emptyMap();

    public ElytraComponentReloadListener() {
        super(GSON, "elytra_components");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        REGISTRY.clear();

        // 1. 先加载代码注册的定义（优先级最高）
        REGISTRY.putAll(CODE_REGISTRY);

        // 2. 解析所有 JSON 定义
        for (var entry : data.entrySet()) {
            try {
                JsonObject root = entry.getValue().getAsJsonObject();
                JsonArray components = root.getAsJsonArray("elytra_components");
                if (components == null) {
                    LOGGER.warn("Missing 'elytra_components' array in {}", entry.getKey());
                    continue;
                }
                for (JsonElement elem : components) {
                    ElytraComponentDefinition def = parseDefinition(elem.getAsJsonObject());
                    if (def != null) {
                        // JSON 定义不能覆盖代码注册的定义
                        if (!CODE_REGISTRY.containsKey(def.componentId())) {
                            REGISTRY.put(def.componentId(), def);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error loading elytra_components from {}: {}", entry.getKey(), e.getMessage());
            }
        }

        // 3. 重建物品索引
        rebuildItemIndex();

        LOGGER.info("Loaded {} elytra component definitions ({} from code, {} from datapack)",
                REGISTRY.size(), CODE_REGISTRY.size(), REGISTRY.size() - CODE_REGISTRY.size());
    }

    /**
     * 供其他模组通过代码注册组件定义（优先级高于 JSON）
     */
    public static synchronized void registerDirectly(String componentId, ElytraComponentDefinition def) {
        CODE_REGISTRY.put(componentId, def);
        // 如果已经 reload 过，需要更新索引
        if (!REGISTRY.isEmpty()) {
            REGISTRY.put(componentId, def);
            rebuildItemIndex();
        }
    }

    /**
     * 通过组件 ID 获取定义
     */
    @Nullable
    public static ElytraComponentDefinition getDefinition(String id) {
        return REGISTRY.get(id);
    }

    /**
     * 获取所有已注册的定义
     */
    public static Collection<ElytraComponentDefinition> getAll() {
        return REGISTRY.values();
    }

    /**
     * 通过原版物品 ID 查找定义（反向查询）
     */
    @Nullable
    public static ElytraComponentDefinition findByItem(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return ITEM_INDEX.get(id);
    }

    /**
     * 通过 ResourceLocation 查找
     */
    @Nullable
    public static ElytraComponentDefinition findByItemId(ResourceLocation itemId) {
        return ITEM_INDEX.get(itemId);
    }

    /**
     * 判断某个物品是否是已注册的鞘翅组件
     */
    public static boolean isRegisteredElytra(Item item) {
        return findByItem(item) != null;
    }

    public static boolean isRegisteredElytra(ResourceLocation itemId) {
        return ITEM_INDEX.containsKey(itemId);
    }

    /**
     * 清空所有注册（主要用于测试）
     */
    public static synchronized void clear() {
        REGISTRY.clear();
        CODE_REGISTRY.clear();
        ITEM_INDEX = Collections.emptyMap();
    }

    // ==================== 内部辅助方法 ====================

/**
 * 重建物品索引的方法
 * 此方法会遍历注册表中的所有值，并将它们映射到对应的物品上
 * 最终创建一个不可修改的映射表，用于快速查找物品对应的组件定义
 */
    private static void rebuildItemIndex() {
        // 创建一个新的HashMap用于存储物品索引
        Map<ResourceLocation, ElytraComponentDefinition> index = new HashMap<>();
        // 遍历注册表中的所有值
        for (ElytraComponentDefinition def : REGISTRY.values()) {
            // 将物品作为键，组件定义作为值存入索引
            index.put(def.elytraItem(), def);
        }
        // 将创建的索引设置为不可修改的映射表，并赋值给ITEM_INDEX
        ITEM_INDEX = Collections.unmodifiableMap(index);
    }

    @Nullable
    private static ElytraComponentDefinition parseDefinition(JsonObject obj) {
        try {
            // component_id (必需)
            String componentId = getString(obj, "component_id");
            if (componentId == null) {
                LOGGER.warn("Skipping definition without component_id");
                return null;
            }

            // elytra_item (必需)
            String elytraItemStr = getString(obj, "elytra_item");
            if (elytraItemStr == null) {
                LOGGER.warn("Skipping definition '{}' without elytra_item", componentId);
                return null;
            }
            ResourceLocation elytraItem = ResourceLocation.parse(elytraItemStr);

            // texture (可选)
            ElytraComponentDefinition.TextureInfo texture = parseTexture(obj.getAsJsonObject("texture"));

            // durability (必需)
            JsonObject durabilityObj = obj.getAsJsonObject("durability");
            if (durabilityObj == null) {
                LOGGER.warn("Skipping definition '{}' without durability", componentId);
                return null;
            }
            int base = getInt(durabilityObj, "base", 432);
            float multiplier = getFloat(durabilityObj, "multiplier", 1.0f);
            int max = getInt(durabilityObj, "max", 0);
            ElytraComponentDefinition.DurabilityInfo durability = new ElytraComponentDefinition.DurabilityInfo(base, multiplier, max);

            // render (可选)
            ElytraComponentDefinition.RenderInfo render = parseRender(obj.getAsJsonObject("render"));

            // compatibility (可选)
            ElytraComponentDefinition.CompatibilityInfo compatibility = parseCompatibility(obj.getAsJsonObject("compatibility"));

            // tags (可选)
            List<String> tags = parseStringList(obj.getAsJsonArray("tags"));

            return new ElytraComponentDefinition(
                    componentId, elytraItem, texture, durability, render, compatibility, tags
            );
        } catch (Exception e) {
            LOGGER.error("Failed to parse elytra component definition: {}", e.getMessage());
            return null;
        }
    }

    @Nullable
    private static ElytraComponentDefinition.TextureInfo parseTexture(@Nullable JsonObject obj) {
        if (obj == null) return null;
        ResourceLocation layer = parseResourceLocation(obj, "elytra_layer");
        ResourceLocation glow = parseResourceLocation(obj, "elytra_layer_glow");
        ResourceLocation overlay = parseResourceLocation(obj, "elytra_layer_overlay");
        if (layer == null && glow == null && overlay == null) return null;
        return new ElytraComponentDefinition.TextureInfo(layer, glow, overlay);
    }

    private static ElytraComponentDefinition.RenderInfo parseRender(@Nullable JsonObject obj) {
        if (obj == null) return new ElytraComponentDefinition.RenderInfo(null, false, null);
        String tint = getString(obj, "tint_color");
        boolean glow = getBoolean(obj, "has_glow", false);
        String glowColor = getString(obj, "glow_color");
        return new ElytraComponentDefinition.RenderInfo(tint, glow, glowColor);
    }

    private static ElytraComponentDefinition.CompatibilityInfo parseCompatibility(@Nullable JsonObject obj) {
        if (obj == null) return new ElytraComponentDefinition.CompatibilityInfo(null, null);
        List<String> required = parseStringList(obj.getAsJsonArray("required_mods"));
        List<String> incompatible = parseStringList(obj.getAsJsonArray("incompatible_with"));
        return new ElytraComponentDefinition.CompatibilityInfo(required, incompatible);
    }

    @Nullable
    private static ResourceLocation parseResourceLocation(JsonObject obj, String key) {
        String value = getString(obj, key);
        return value != null ? ResourceLocation.parse(value) : null;
    }

    @Nullable
    private static String getString(JsonObject obj, String key) {
        JsonElement elem = obj.get(key);
        return elem != null && !elem.isJsonNull() ? elem.getAsString() : null;
    }

    private static int getInt(JsonObject obj, String key, int defaultValue) {
        JsonElement elem = obj.get(key);
        return elem != null && !elem.isJsonNull() ? elem.getAsInt() : defaultValue;
    }

    private static float getFloat(JsonObject obj, String key, float defaultValue) {
        JsonElement elem = obj.get(key);
        return elem != null && !elem.isJsonNull() ? elem.getAsFloat() : defaultValue;
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        JsonElement elem = obj.get(key);
        return elem != null && !elem.isJsonNull() ? elem.getAsBoolean() : defaultValue;
    }

    private static List<String> parseStringList(@Nullable JsonArray array) {
        if (array == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (JsonElement elem : array) {
            if (elem != null && !elem.isJsonNull()) {
                result.add(elem.getAsString());
            }
        }
        return result;
    }
}

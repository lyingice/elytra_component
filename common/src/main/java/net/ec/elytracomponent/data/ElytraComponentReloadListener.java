package net.ec.elytracomponent.data;

import com.google.gson.*;
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
 * 数据包资源重载监听器，读取 data/<namespace>/elytra_components 下的 JSON 文件。
 * 纯 Minecraft API 依赖，可在 common 模块中共享。
 */
public class ElytraComponentReloadListener extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger(ElytraComponentReloadListener.class);

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    private static final Map<String, ElytraComponentDefinition> REGISTRY = new HashMap<>();
    private static final Map<String, ElytraComponentDefinition> CODE_REGISTRY = new HashMap<>();
    private static Map<ResourceLocation, ElytraComponentDefinition> ITEM_INDEX = Collections.emptyMap();

    public ElytraComponentReloadListener() {
        super(GSON, "elytra_components");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        REGISTRY.clear();
        ITEM_INDEX = Collections.emptyMap();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            try {
                ElytraComponentDefinition def = parseDefinition(fileId, entry.getValue().getAsJsonObject());
                if (def != null) {
                    REGISTRY.put(def.componentId(), def);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load elytra component definition from {}: {}", fileId, e.getMessage());
            }
        }

        CODE_REGISTRY.forEach((id, def) -> {
            REGISTRY.put(id, def);
            LOGGER.info("Overridden component '{}' from code registration", id);
        });

        rebuildIndex();
        LOGGER.info("Loaded {} elytra component definitions ({} code-registered)", REGISTRY.size(), CODE_REGISTRY.size());
    }

    @Nullable
    private ElytraComponentDefinition parseDefinition(ResourceLocation fileId, JsonObject json) {
        if (!json.has("elytra_item")) return null;

        String componentId = fileId.getPath().replace(".json", "").replace("/", ".");
        ResourceLocation elytraItem = new ResourceLocation(json.get("elytra_item").getAsString());

        ElytraComponentDefinition.TextureInfo texture = null;
        if (json.has("texture") && json.get("texture").isJsonObject()) {
            JsonObject texJson = json.getAsJsonObject("texture");
            ResourceLocation layer = texJson.has("elytra_layer") ? new ResourceLocation(texJson.get("elytra_layer").getAsString()) : null;
            ResourceLocation glow = texJson.has("elytra_layer_glow") ? new ResourceLocation(texJson.get("elytra_layer_glow").getAsString()) : null;
            ResourceLocation overlay = texJson.has("elytra_layer_overlay") ? new ResourceLocation(texJson.get("elytra_layer_overlay").getAsString()) : null;
            texture = new ElytraComponentDefinition.TextureInfo(layer, glow, overlay);
        }

        JsonObject durJson = json.getAsJsonObject("durability");
        int base = durJson.get("base").getAsInt();
        float multiplier = durJson.has("multiplier") ? durJson.get("multiplier").getAsFloat() : 1.0f;
        int maxDurability = durJson.has("max_durability") ? durJson.get("max_durability").getAsInt() : Integer.MAX_VALUE;
        ElytraComponentDefinition.DurabilityInfo durability = new ElytraComponentDefinition.DurabilityInfo(base, multiplier, maxDurability);

        ElytraComponentDefinition.RenderInfo render = null;
        if (json.has("render") && json.get("render").isJsonObject()) {
            JsonObject renJson = json.getAsJsonObject("render");
            String tintColor = renJson.has("tint_color") ? renJson.get("tint_color").getAsString() : null;
            boolean hasGlow = renJson.has("has_glow") && renJson.get("has_glow").getAsBoolean();
            String glowColor = renJson.has("glow_color") ? renJson.get("glow_color").getAsString() : null;
            render = new ElytraComponentDefinition.RenderInfo(tintColor, hasGlow, glowColor);
        }

        ElytraComponentDefinition.CompatibilityInfo compat = null;
        if (json.has("compatibility") && json.get("compatibility").isJsonObject()) {
            JsonObject compJson = json.getAsJsonObject("compatibility");
            List<String> required = compJson.has("required_mods") ? parseStringList(compJson.getAsJsonArray("required_mods")) : List.of();
            List<String> incompatible = compJson.has("incompatible_with") ? parseStringList(compJson.getAsJsonArray("incompatible_with")) : List.of();
            compat = new ElytraComponentDefinition.CompatibilityInfo(required, incompatible);
        }

        List<String> tags = json.has("tags") ? parseStringList(json.getAsJsonArray("tags")) : List.of();

        return new ElytraComponentDefinition(componentId, elytraItem, texture, durability, render, compat, tags);
    }

    private List<String> parseStringList(JsonArray array) {
        List<String> result = new ArrayList<>();
        for (JsonElement e : array) result.add(e.getAsString());
        return result;
    }

    private void rebuildIndex() {
        Map<ResourceLocation, ElytraComponentDefinition> index = new HashMap<>();
        for (ElytraComponentDefinition def : REGISTRY.values()) {
            index.put(def.elytraItem(), def);
        }
        ITEM_INDEX = Map.copyOf(index);
    }

    public static void registerDirectly(String componentId, ElytraComponentDefinition def) {
        CODE_REGISTRY.put(componentId, def);
    }

    public static boolean isRegisteredElytra(Item item) {
        return ITEM_INDEX.containsKey(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    public static ElytraComponentDefinition findByItem(Item item) {
        return ITEM_INDEX.get(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    public static ElytraComponentDefinition getDefinition(String componentId) {
        return REGISTRY.get(componentId);
    }

    public static Collection<ElytraComponentDefinition> getAll() {
        return REGISTRY.values();
    }
}

package net.ec.elytracomponent.client;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimPattern;
import java.util.Map;
import java.util.HashMap;

public class ElytraTrimTextures {

    private static final Map<ResourceLocation, ResourceLocation> TRIM_TEXTURES = new HashMap<>();

    public static void init() {
        // 原版10种纹饰
        register("sentry", "sentry");
        register("dune", "dune");
        register("coast", "coast");
        register("wild", "wild");
        register("ward", "ward");
        register("eye", "eye");
        register("vex", "vex");
        register("tide", "tide");
        register("snout", "snout");
        register("rib", "rib");
        register("silence", "silence");
        // 1.20 考古加入
        register("wayfinder", "wayfinder");
        register("shaper", "shaper");
        register("raiser", "raiser");
        register("host", "host");

        // 1.21 试炼密室
        register("bolt", "bolt");
        register("flow", "flow");
        // 1.21 下界
        register("spire", "spire");
    }

    private static void register(String patternName, String textureName) {
        TRIM_TEXTURES.put(
                ResourceLocation.withDefaultNamespace(patternName),
                ResourceLocation.fromNamespaceAndPath(ElytraComponentMod.MODID,
                        "textures/models/elytra/" + textureName + ".png")
        );
    }

    public static ResourceLocation getTexture(ResourceLocation patternId) {
        return TRIM_TEXTURES.getOrDefault(patternId, null);
    }

    /**
     * API：供其他模组注册自定义纹饰纹理
     */
    public static void registerCustom(ResourceLocation patternId, ResourceLocation texture) {
        TRIM_TEXTURES.put(patternId, texture);
    }

    public static Map<ResourceLocation, ResourceLocation> getAll() {
        return new HashMap<>(TRIM_TEXTURES);
    }
}
package net.ec.elytracomponent.data;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * 从 elytra_components 数据包 JSON 解析出的鞘翅组件定义。
 */
public record ElytraComponentDefinition(
        String componentId,
        ResourceLocation elytraItem,
        @Nullable TextureInfo texture,
        DurabilityInfo durability,
        RenderInfo render,
        CompatibilityInfo compatibility,
        List<String> tags
) {
    public record TextureInfo(
            @Nullable ResourceLocation elytraLayer,
            @Nullable ResourceLocation elytraLayerGlow,
            @Nullable ResourceLocation elytraLayerOverlay
    ) {}

    public record DurabilityInfo(int base, float multiplier, int maxDurability) {
        public DurabilityInfo(int base, float multiplier, int maxDurability) {
            this.base = base;
            this.multiplier = multiplier;
            this.maxDurability = maxDurability > 0 ? maxDurability : Integer.MAX_VALUE;
        }

        public int calculateDurability(int originalMaxDurability) {
            int calculated = Math.round(base * multiplier);
            if (originalMaxDurability > calculated) calculated = originalMaxDurability;
            return Math.min(calculated, maxDurability);
        }
    }

    public record RenderInfo(
            @Nullable String tintColor,
            boolean hasGlow,
            @Nullable String glowColor
    ) {}

    public record CompatibilityInfo(
            List<String> requiredMods,
            List<String> incompatibleWith
    ) {
        public CompatibilityInfo {
            if (requiredMods == null) requiredMods = Collections.emptyList();
            if (incompatibleWith == null) incompatibleWith = Collections.emptyList();
        }
    }

    public ResourceLocation getSourceNamespace() {
        return elytraItem;
    }
}

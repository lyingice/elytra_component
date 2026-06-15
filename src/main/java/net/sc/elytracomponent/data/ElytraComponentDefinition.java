package net.sc.elytracomponent.data;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * 从 elytra_components.json 解析出的鞘翅组件定义。
 * 每个定义代表一个可安装的鞘翅类型。
 */
public record ElytraComponentDefinition(
        // 唯一标识符，用于配方引用
        String componentId,
        // 原鞘翅物品ID，如 "twilightforest:fiery_elytra"
        ResourceLocation elytraItem,
        // 纹理配置
        @Nullable TextureInfo texture,
        // 耐久配置
        DurabilityInfo durability,
        // 渲染配置
        RenderInfo render,
        // 兼容性配置
        CompatibilityInfo compatibility,
        // 功能标签
        List<String> tags
) {

    public record TextureInfo(
            @Nullable ResourceLocation elytraLayer,
            @Nullable ResourceLocation elytraLayerGlow,
            @Nullable ResourceLocation elytraLayerOverlay
    ) {}

    public record DurabilityInfo(
            int base,
            float multiplier,
            int maxDurability
    ) {
        public DurabilityInfo(int base, float multiplier, int maxDurability) {
            this.base = base;
            this.multiplier = multiplier;
            this.maxDurability = maxDurability > 0 ? maxDurability : Integer.MAX_VALUE;
        }

        /** 计算最终耐久值 */
        public int calculateDurability(int originalMaxDurability) {
            int calculated = Math.round(base * multiplier);
            // 如果原鞘翅有更高的耐久，使用原鞘翅的
            if (originalMaxDurability > calculated) {
                calculated = originalMaxDurability;
            }
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
        return new ResourceLocation(
                elytraItem.getNamespace(),
                "elytra_component"
        );
    }
}

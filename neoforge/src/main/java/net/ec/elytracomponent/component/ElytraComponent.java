package net.ec.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

/**
 * 鞘翅组件数据记录。
 * source、durability、config 等信息以子记录形式组织。
 */
public record ElytraComponent(
        SourceInfo source,
        int currentDurability,
        int maxDurability,
        @Nullable ResourceLocation textureOverride,
        @Nullable CompoundTag extraData,
        @Nullable CompoundTag originalChestAttributes,
        @Nullable CompoundTag abilityConfig,
        @Nullable CompoundTag particleConfig
) {

    public record SourceInfo(
            ResourceLocation sourceNamespace,
            ResourceLocation originalElytraId,
            @Nullable CompoundTag originalElytraTag
    ) {}

    /** 只变更耐久，其他字段不变 */
    public ElytraComponent withDurability(int newCurrent) {
        return new ElytraComponent(source, newCurrent, maxDurability, textureOverride,
                extraData, originalChestAttributes, abilityConfig, particleConfig);
    }

    /** 只变更纹理，其他字段不变 */
    public ElytraComponent withTexture(@Nullable ResourceLocation texture) {
        return new ElytraComponent(source, currentDurability, maxDurability,
                texture, extraData, originalChestAttributes, abilityConfig, particleConfig);
    }
}

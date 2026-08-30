package net.ec.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

/**
 * 鞘翅组件数据记录。
 * 与 NeoForge 版结构完全一致，通过 ElytraComponentNbtHelper 做 NBT 序列化。
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

    public ElytraComponent withDurability(int newCurrent) {
        return new ElytraComponent(source, newCurrent, maxDurability, textureOverride,
                extraData, originalChestAttributes, abilityConfig, particleConfig);
    }

    public ElytraComponent withTexture(@Nullable ResourceLocation texture) {
        return new ElytraComponent(source, currentDurability, maxDurability,
                texture, extraData, originalChestAttributes, abilityConfig, particleConfig);
    }
}

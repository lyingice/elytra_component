package net.ec.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * 鞘翅组件数据记录。
 * 纯数据载体，不含平台特定的序列化。
 * 序列化由各平台模块的 ModComponents/Codecs 类处理。
 */
public record ElytraComponent(
        ResourceLocation sourceNamespace,
        ResourceLocation originalElytraId,
        @Nullable CompoundTag originalElytraTag,
        int currentDurability,
        int maxDurability,
        @Nullable ResourceLocation textureOverride,
        @Nullable CompoundTag extraData,
        @Nullable CompoundTag originalChestAttributes,
        @Nullable CompoundTag abilityConfig,
        @Nullable CompoundTag particleConfig
) {}

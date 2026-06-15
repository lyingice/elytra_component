package net.sc.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record ElytraComponent(
        ResourceLocation sourceNamespace,
        ResourceLocation originalElytraId,
        @Nullable CompoundTag originalElytraTag,
        int currentDurability,
        int maxDurability,
        @Nullable ResourceLocation textureOverride,
        @Nullable CompoundTag extraData,
        // === 新增 ===
        @Nullable CompoundTag originalChestAttributes,
        @Nullable CompoundTag abilityConfig,
        @Nullable CompoundTag particleConfig
) {

    private static final String KEY_SOURCE_NAMESPACE = "source_namespace";
    private static final String KEY_ORIGINAL_ELYTRA_ID = "original_elytra_id";
    private static final String KEY_ORIGINAL_ELYTRA_TAG = "original_elytra_tag";
    private static final String KEY_CURRENT_DURABILITY = "current_durability";
    private static final String KEY_MAX_DURABILITY = "max_durability";
    private static final String KEY_TEXTURE_OVERRIDE = "texture_override";
    private static final String KEY_EXTRA_DATA = "extra_data";
    private static final String KEY_ORIGINAL_CHEST_ATTRIBUTES = "original_chest_attributes";
    private static final String KEY_ABILITY_CONFIG = "ability_config";
    private static final String KEY_PARTICLE_CONFIG = "particle_config";

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_SOURCE_NAMESPACE, sourceNamespace.toString());
        tag.putString(KEY_ORIGINAL_ELYTRA_ID, originalElytraId.toString());
        if (originalElytraTag != null) tag.put(KEY_ORIGINAL_ELYTRA_TAG, originalElytraTag.copy());
        tag.putInt(KEY_CURRENT_DURABILITY, currentDurability);
        tag.putInt(KEY_MAX_DURABILITY, maxDurability);
        if (textureOverride != null) tag.putString(KEY_TEXTURE_OVERRIDE, textureOverride.toString());
        if (extraData != null) tag.put(KEY_EXTRA_DATA, extraData.copy());
        if (originalChestAttributes != null) tag.put(KEY_ORIGINAL_CHEST_ATTRIBUTES, originalChestAttributes.copy());
        if (abilityConfig != null) tag.put(KEY_ABILITY_CONFIG, abilityConfig.copy());
        if (particleConfig != null) tag.put(KEY_PARTICLE_CONFIG, particleConfig.copy());
        return tag;
    }

    @Nullable
    public static ElytraComponent fromNBT(CompoundTag tag) {
        if (tag == null || !tag.contains(KEY_SOURCE_NAMESPACE) || !tag.contains(KEY_ORIGINAL_ELYTRA_ID)) return null;
        try {
            ResourceLocation sourceNamespace = new ResourceLocation(tag.getString(KEY_SOURCE_NAMESPACE));
            ResourceLocation originalElytraId = new ResourceLocation(tag.getString(KEY_ORIGINAL_ELYTRA_ID));
            CompoundTag originalTag = tag.contains(KEY_ORIGINAL_ELYTRA_TAG) ? tag.getCompound(KEY_ORIGINAL_ELYTRA_TAG) : null;
            int currentDurability = tag.getInt(KEY_CURRENT_DURABILITY);
            int maxDurability = tag.getInt(KEY_MAX_DURABILITY);
            ResourceLocation textureOverride = tag.contains(KEY_TEXTURE_OVERRIDE) ? new ResourceLocation(tag.getString(KEY_TEXTURE_OVERRIDE)) : null;
            CompoundTag extraData = tag.contains(KEY_EXTRA_DATA) ? tag.getCompound(KEY_EXTRA_DATA) : null;
            CompoundTag originalChestAttributes = tag.contains(KEY_ORIGINAL_CHEST_ATTRIBUTES) ? tag.getCompound(KEY_ORIGINAL_CHEST_ATTRIBUTES) : null;
            CompoundTag abilityConfig = tag.contains(KEY_ABILITY_CONFIG) ? tag.getCompound(KEY_ABILITY_CONFIG) : null;
            CompoundTag particleConfig = tag.contains(KEY_PARTICLE_CONFIG) ? tag.getCompound(KEY_PARTICLE_CONFIG) : null;
            return new ElytraComponent(sourceNamespace, originalElytraId, originalTag,
                    currentDurability, maxDurability, textureOverride, extraData,
                    originalChestAttributes, abilityConfig, particleConfig);
        } catch (Exception e) {
            return null;
        }
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(sourceNamespace);
        buf.writeResourceLocation(originalElytraId);
        buf.writeNbt(originalElytraTag);
        buf.writeVarInt(currentDurability);
        buf.writeVarInt(maxDurability);
        buf.writeBoolean(textureOverride != null);
        if (textureOverride != null) buf.writeResourceLocation(textureOverride);
        buf.writeNbt(extraData);
        buf.writeNbt(originalChestAttributes);
        buf.writeNbt(abilityConfig);
        buf.writeNbt(particleConfig);
    }

    public static ElytraComponent fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation sourceNamespace = buf.readResourceLocation();
        ResourceLocation originalElytraId = buf.readResourceLocation();
        CompoundTag originalTag = buf.readNbt();
        int currentDurability = buf.readVarInt();
        int maxDurability = buf.readVarInt();
        ResourceLocation textureOverride = buf.readBoolean() ? buf.readResourceLocation() : null;
        CompoundTag extraData = buf.readNbt();
        CompoundTag originalChestAttributes = buf.readNbt();
        CompoundTag abilityConfig = buf.readNbt();
        CompoundTag particleConfig = buf.readNbt();
        return new ElytraComponent(sourceNamespace, originalElytraId, originalTag,
                currentDurability, maxDurability, textureOverride, extraData,
                originalChestAttributes, abilityConfig, particleConfig);
    }
}
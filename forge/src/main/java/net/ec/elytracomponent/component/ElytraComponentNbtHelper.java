package net.ec.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 平台：为共有的 ElytraComponent record 提供 NBT/网络序列化。
 */
public class ElytraComponentNbtHelper {

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

    public static CompoundTag toNBT(ElytraComponent c) {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_SOURCE_NAMESPACE, c.source().sourceNamespace().toString());
        tag.putString(KEY_ORIGINAL_ELYTRA_ID, c.source().originalElytraId().toString());
        if (c.source().originalElytraTag() != null) tag.put(KEY_ORIGINAL_ELYTRA_TAG, c.source().originalElytraTag().copy());
        tag.putInt(KEY_CURRENT_DURABILITY, c.currentDurability());
        tag.putInt(KEY_MAX_DURABILITY, c.maxDurability());
        if (c.textureOverride() != null) tag.putString(KEY_TEXTURE_OVERRIDE, c.textureOverride().toString());
        if (c.extraData() != null) tag.put(KEY_EXTRA_DATA, c.extraData().copy());
        if (c.originalChestAttributes() != null) tag.put(KEY_ORIGINAL_CHEST_ATTRIBUTES, c.originalChestAttributes().copy());
        if (c.abilityConfig() != null) tag.put(KEY_ABILITY_CONFIG, c.abilityConfig().copy());
        if (c.particleConfig() != null) tag.put(KEY_PARTICLE_CONFIG, c.particleConfig().copy());
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
            return new ElytraComponent(new ElytraComponent.SourceInfo(sourceNamespace, originalElytraId, originalTag), currentDurability, maxDurability, textureOverride, extraData,
                    originalChestAttributes, abilityConfig, particleConfig);
        } catch (Exception e) {
            return null;
        }
    }

    public static void toNetwork(FriendlyByteBuf buf, ElytraComponent c) {
        buf.writeResourceLocation(c.source().sourceNamespace());
        buf.writeResourceLocation(c.source().originalElytraId());
        buf.writeNbt(c.source().originalElytraTag());
        buf.writeVarInt(c.currentDurability());
        buf.writeVarInt(c.maxDurability());
        buf.writeBoolean(c.textureOverride() != null);
        if (c.textureOverride() != null) buf.writeResourceLocation(c.textureOverride());
        buf.writeNbt(c.extraData());
        buf.writeNbt(c.originalChestAttributes());
        buf.writeNbt(c.abilityConfig());
        buf.writeNbt(c.particleConfig());
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
        return new ElytraComponent(new ElytraComponent.SourceInfo(sourceNamespace, originalElytraId, originalTag), currentDurability, maxDurability, textureOverride, extraData,
                originalChestAttributes, abilityConfig, particleConfig);
    }
}



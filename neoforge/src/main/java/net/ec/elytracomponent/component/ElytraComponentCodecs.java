package net.ec.elytracomponent.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * NeoForge 平台：为共有的 ElytraComponent record 提供 Codec 和 StreamCodec。
 */
public class ElytraComponentCodecs {

    public static final Codec<ElytraComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("source_namespace").forGetter(c -> c.source().sourceNamespace()),
                    ResourceLocation.CODEC.fieldOf("original_elytra_id").forGetter(c -> c.source().originalElytraId()),
                    CompoundTag.CODEC.optionalFieldOf("original_elytra_tag").forGetter(c -> Optional.ofNullable(c.source().originalElytraTag())),
                    Codec.INT.fieldOf("current_durability").forGetter(ElytraComponent::currentDurability),
                    Codec.INT.fieldOf("max_durability").forGetter(ElytraComponent::maxDurability),
                    ResourceLocation.CODEC.optionalFieldOf("texture_override").forGetter(c -> Optional.ofNullable(c.textureOverride())),
                    CompoundTag.CODEC.optionalFieldOf("extra_data").forGetter(c -> Optional.ofNullable(c.extraData())),
                    CompoundTag.CODEC.optionalFieldOf("original_chest_attributes").forGetter(c -> Optional.ofNullable(c.originalChestAttributes())),
                    CompoundTag.CODEC.optionalFieldOf("ability_config").forGetter(c -> Optional.ofNullable(c.abilityConfig())),
                    CompoundTag.CODEC.optionalFieldOf("particle_config").forGetter(c -> Optional.ofNullable(c.particleConfig()))
            ).apply(instance, (sourceNamespace, originalElytraId, originalElytraTag,
                               currentDurability, maxDurability,
                               textureOverrideOpt, extraDataOpt,
                               originalChestAttributesOpt, abilityConfigOpt, particleConfigOpt) ->
                    new ElytraComponent(new ElytraComponent.SourceInfo(sourceNamespace, originalElytraId, originalElytraTag.orElse(null)), currentDurability, maxDurability,
                            textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                            originalChestAttributesOpt.orElse(null),
                            abilityConfigOpt.orElse(null), particleConfigOpt.orElse(null))
            )
    );

    public static final StreamCodec<ByteBuf, ElytraComponent> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ElytraComponent decode(ByteBuf buf) {
            ResourceLocation sourceNamespace = ResourceLocation.STREAM_CODEC.decode(buf);
            ResourceLocation originalElytraId = ResourceLocation.STREAM_CODEC.decode(buf);
            CompoundTag originalElytraTag = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf).orElse(null);
            int currentDurability = ByteBufCodecs.VAR_INT.decode(buf);
            int maxDurability = ByteBufCodecs.VAR_INT.decode(buf);
            Optional<ResourceLocation> textureOverrideOpt = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
            Optional<CompoundTag> extraDataOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            Optional<CompoundTag> originalChestAttributesOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            Optional<CompoundTag> abilityConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            Optional<CompoundTag> particleConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            return new ElytraComponent(new ElytraComponent.SourceInfo(sourceNamespace, originalElytraId, originalElytraTag), currentDurability, maxDurability,
                    textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                    originalChestAttributesOpt.orElse(null),
                    abilityConfigOpt.orElse(null), particleConfigOpt.orElse(null));
        }

        @Override
        public void encode(ByteBuf buf, ElytraComponent c) {
            ResourceLocation.STREAM_CODEC.encode(buf, c.source().sourceNamespace());
            ResourceLocation.STREAM_CODEC.encode(buf, c.source().originalElytraId());
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.source().originalElytraTag()));
            ByteBufCodecs.VAR_INT.encode(buf, c.currentDurability());
            ByteBufCodecs.VAR_INT.encode(buf, c.maxDurability());
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, Optional.ofNullable(c.textureOverride()));
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.extraData()));
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.originalChestAttributes()));
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.abilityConfig()));
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.particleConfig()));
        }
    };
}



package net.ec.elytracomponent.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 核心数据组件，存储在胸甲的 ItemStack 中。
 * 封装了原鞘翅的所有数据，支持序列化、网络同步。
 */
public record ElytraComponent(
        // 来源模组ID
        ResourceLocation sourceNamespace,
        // 原鞘翅物品ID
        ResourceLocation originalElytraId,
        // 原鞘翅的所有 DataComponent（用于拆卸时100%还原）
        DataComponentMap originalElytraComponents,
        // 当前耐久
        int currentDurability,
        // 最大耐久
        int maxDurability,
        // 可选：覆盖纹理路径
        @Nullable ResourceLocation textureOverride,
        // 可选：额外 NBT 数据（供未来扩展）
        @Nullable CompoundTag extraData,

        // === 属性修饰符（安装时合并到胸甲，拆卸时恢复） ===
        @Nullable CompoundTag originalChestAttributes,

        // === 未来扩展预留 ===
        // 特殊能力配置
        @Nullable CompoundTag abilityConfig,
        // 自定义粒子效果
        @Nullable CompoundTag particleConfig
) {

    public static final Codec<ElytraComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("source_namespace")
                            .forGetter(ElytraComponent::sourceNamespace),
                    ResourceLocation.CODEC
                            .fieldOf("original_elytra_id")
                            .forGetter(ElytraComponent::originalElytraId),
                    DataComponentMap.CODEC
                            .fieldOf("original_elytra_components")
                            .forGetter(ElytraComponent::originalElytraComponents),
                    Codec.INT
                            .fieldOf("current_durability")
                            .forGetter(ElytraComponent::currentDurability),
                    Codec.INT
                            .fieldOf("max_durability")
                            .forGetter(ElytraComponent::maxDurability),
                    ResourceLocation.CODEC
                            .optionalFieldOf("texture_override")
                            .forGetter(c -> Optional.ofNullable(c.textureOverride)),
                    CompoundTag.CODEC
                            .optionalFieldOf("extra_data")
                            .forGetter(c -> Optional.ofNullable(c.extraData)),
                    CompoundTag.CODEC
                            .optionalFieldOf("original_chest_attributes")
                            .forGetter(c -> Optional.ofNullable(c.originalChestAttributes)),
                    CompoundTag.CODEC
                            .optionalFieldOf("ability_config")
                            .forGetter(c -> Optional.ofNullable(c.abilityConfig)),
                    CompoundTag.CODEC
                            .optionalFieldOf("particle_config")
                            .forGetter(c -> Optional.ofNullable(c.particleConfig))
            ).apply(instance, (sourceNamespace, originalElytraId, originalElytraComponents,
                               currentDurability, maxDurability,
                               textureOverrideOpt, extraDataOpt,
                               originalChestAttributesOpt, abilityConfigOpt, particleConfigOpt) ->
                    new ElytraComponent(
                            sourceNamespace, originalElytraId, originalElytraComponents,
                            currentDurability, maxDurability,
                            textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                            originalChestAttributesOpt.orElse(null),
                            abilityConfigOpt.orElse(null),
                            particleConfigOpt.orElse(null)
                    )
            )
    );

    public static final StreamCodec<ByteBuf, ElytraComponent> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ElytraComponent decode(ByteBuf buf) {
            ResourceLocation sourceNamespace = ResourceLocation.STREAM_CODEC.decode(buf);
            ResourceLocation originalElytraId = ResourceLocation.STREAM_CODEC.decode(buf);
            DataComponentMap originalElytraComponents = ByteBufCodecs.fromCodec(DataComponentMap.CODEC).decode(buf);
            int currentDurability = ByteBufCodecs.VAR_INT.decode(buf);
            int maxDurability = ByteBufCodecs.VAR_INT.decode(buf);
            Optional<ResourceLocation> textureOverrideOpt = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
            Optional<CompoundTag> extraDataOpt = ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).decode(buf);
            Optional<CompoundTag> originalChestAttributesOpt = ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).decode(buf);
            Optional<CompoundTag> abilityConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).decode(buf);
            Optional<CompoundTag> particleConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).decode(buf);

            return new ElytraComponent(
                    sourceNamespace, originalElytraId, originalElytraComponents,
                    currentDurability, maxDurability,
                    textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                    originalChestAttributesOpt.orElse(null),
                    abilityConfigOpt.orElse(null),
                    particleConfigOpt.orElse(null)
            );
        }

        @Override
        public void encode(ByteBuf buf, ElytraComponent c) {
            ResourceLocation.STREAM_CODEC.encode(buf, c.sourceNamespace());
            ResourceLocation.STREAM_CODEC.encode(buf, c.originalElytraId());
            ByteBufCodecs.fromCodec(DataComponentMap.CODEC).encode(buf, c.originalElytraComponents());
            ByteBufCodecs.VAR_INT.encode(buf, c.currentDurability());
            ByteBufCodecs.VAR_INT.encode(buf, c.maxDurability());
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, Optional.ofNullable(c.textureOverride()));
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).encode(buf, Optional.ofNullable(c.extraData()));
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).encode(buf, Optional.ofNullable(c.originalChestAttributes()));
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).encode(buf, Optional.ofNullable(c.abilityConfig()));
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(CompoundTag.CODEC)).encode(buf, Optional.ofNullable(c.particleConfig()));
        }
    };
}
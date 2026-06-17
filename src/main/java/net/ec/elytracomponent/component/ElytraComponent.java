package net.ec.elytracomponent.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * ElytraComponent 类是一个记录类(Record)，用于存储和管理鞘翅(Elytra)的相关属性和数据。
 * 它包含了鞘翅的命名空间、ID、耐久度、纹理覆盖以及各种配置信息。
 */
public record ElytraComponent(
        // 鞘翅资源的命名空间
        ResourceLocation sourceNamespace,
        // 原始鞘翅的ID
        ResourceLocation originalElytraId,
        // 原始鞘翅的NBT标签数据
        @Nullable CompoundTag originalElytraTag,  // ← 改回 NBT
        // 当前耐久度
        int currentDurability,
        // 最大耐久度
        int maxDurability,
        // 纹理覆盖（可选）
        @Nullable ResourceLocation textureOverride,
        // 额外数据（可选）
        @Nullable CompoundTag extraData,
        // 原始胸部属性（可选）
        @Nullable CompoundTag originalChestAttributes,
        // 能力配置（可选）
        @Nullable CompoundTag abilityConfig,
        // 粒子效果配置（可选）
        @Nullable CompoundTag particleConfig
) {

    /**
     * 用于序列化和反序列化ElytraComponent的Codec实例。
     * 使用RecordCodecBuilder来构建，可以方便地将ElytraComponent转换为数据包或从数据包恢复。
     */
    public static final Codec<ElytraComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 解析/序列化源命名空间
                    ResourceLocation.CODEC
                            .fieldOf("source_namespace")
                            .forGetter(ElytraComponent::sourceNamespace),
                    // 解析/序列化原始鞘翅ID
                    ResourceLocation.CODEC
                            .fieldOf("original_elytra_id")
                            .forGetter(ElytraComponent::originalElytraId),
                    // 解析/序列化原始鞘翅标签（可选）
                    CompoundTag.CODEC
                            .optionalFieldOf("original_elytra_tag")
                            .forGetter(c -> Optional.ofNullable(c.originalElytraTag)),
                    // 解析/序列化当前耐久度
                    Codec.INT
                            .fieldOf("current_durability")
                            .forGetter(ElytraComponent::currentDurability),
                    // 解析/序列化最大耐久度
                    Codec.INT
                            .fieldOf("max_durability")
                            .forGetter(ElytraComponent::maxDurability),
                    // 解析/序列化纹理覆盖（可选）
                    ResourceLocation.CODEC
                            .optionalFieldOf("texture_override")
                            .forGetter(c -> Optional.ofNullable(c.textureOverride)),
                    // 解析/序列化额外数据（可选）
                    CompoundTag.CODEC
                            .optionalFieldOf("extra_data")
                            .forGetter(c -> Optional.ofNullable(c.extraData)),
                    // 解析/序列化原始胸部属性（可选）
                    CompoundTag.CODEC
                            .optionalFieldOf("original_chest_attributes")
                            .forGetter(c -> Optional.ofNullable(c.originalChestAttributes)),
                    // 解析/序列化能力配置（可选）
                    CompoundTag.CODEC
                            .optionalFieldOf("ability_config")
                            .forGetter(c -> Optional.ofNullable(c.abilityConfig)),
                    // 解析/序列化粒子效果配置（可选）
                    CompoundTag.CODEC
                            .optionalFieldOf("particle_config")
                            .forGetter(c -> Optional.ofNullable(c.particleConfig))
            ).apply(instance, (sourceNamespace, originalElytraId, originalElytraTag,
                               currentDurability, maxDurability,
                               textureOverrideOpt, extraDataOpt,
                               originalChestAttributesOpt, abilityConfigOpt, particleConfigOpt) ->
                    new ElytraComponent(
                            sourceNamespace, originalElytraId, originalElytraTag.orElse(null),
                            currentDurability, maxDurability,
                            textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                            originalChestAttributesOpt.orElse(null),
                            abilityConfigOpt.orElse(null),
                            particleConfigOpt.orElse(null)
                    )
            )
    );

    /**
     * 用于网络传输的StreamCodec实例。
     * 负责将ElytraComponent编码到ByteBuf中或从ByteBuf解码。
     */
    public static final StreamCodec<ByteBuf, ElytraComponent> STREAM_CODEC = new StreamCodec<>() {
        /**
         * 从ByteBuf解码ElytraComponent实例。
         * @param buf 包含编码数据的ByteBuf
         * @return 解码后的ElytraComponent实例
         */
        @Override
        public ElytraComponent decode(ByteBuf buf) {
            // 解码源命名空间
            ResourceLocation sourceNamespace = ResourceLocation.STREAM_CODEC.decode(buf);
            // 解码原始鞘翅ID
            ResourceLocation originalElytraId = ResourceLocation.STREAM_CODEC.decode(buf);
            // 解码原始鞘翅标签（可选）
            CompoundTag originalElytraTag = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf).orElse(null);
            // 解码当前耐久度
            int currentDurability = ByteBufCodecs.VAR_INT.decode(buf);
            // 解码最大耐久度
            int maxDurability = ByteBufCodecs.VAR_INT.decode(buf);
            // 解码纹理覆盖（可选）
            Optional<ResourceLocation> textureOverrideOpt = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
            // 解码额外数据（可选）
            Optional<CompoundTag> extraDataOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            // 解码原始胸部属性（可选）
            Optional<CompoundTag> originalChestAttributesOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            // 解码能力配置（可选）
            Optional<CompoundTag> abilityConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);
            // 解码粒子效果配置（可选）
            Optional<CompoundTag> particleConfigOpt = ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).decode(buf);

            // 创建并返回新的ElytraComponent实例
            return new ElytraComponent(
                    sourceNamespace, originalElytraId, originalElytraTag,
                    currentDurability, maxDurability,
                    textureOverrideOpt.orElse(null), extraDataOpt.orElse(null),
                    originalChestAttributesOpt.orElse(null),
                    abilityConfigOpt.orElse(null),
                    particleConfigOpt.orElse(null)
            );
        }

        /**
         * 将ElytraComponent实例编码到ByteBuf中。
         * @param buf 目标ByteBuf
         * @param c 要编码的ElytraComponent实例
         */
        @Override
        public void encode(ByteBuf buf, ElytraComponent c) {
            // 编码源命名空间
            ResourceLocation.STREAM_CODEC.encode(buf, c.sourceNamespace());
            // 编码原始鞘翅ID
            ResourceLocation.STREAM_CODEC.encode(buf, c.originalElytraId());
            // 编码原始鞘翅标签（可选）
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.originalElytraTag()));
            // 编码当前耐久度
            ByteBufCodecs.VAR_INT.encode(buf, c.currentDurability());
            // 编码最大耐久度
            ByteBufCodecs.VAR_INT.encode(buf, c.maxDurability());
            // 编码纹理覆盖（可选）
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, Optional.ofNullable(c.textureOverride()));
            // 编码额外数据（可选）
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.extraData()));
            // 编码原始胸部属性（可选）
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.originalChestAttributes()));
            // 编码能力配置（可选）
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.abilityConfig()));
            // 编码粒子效果配置（可选）
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).encode(buf, Optional.ofNullable(c.particleConfig()));
        }
    };
}
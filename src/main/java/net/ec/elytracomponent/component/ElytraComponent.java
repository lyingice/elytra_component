package net.ec.elytracomponent.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 核心数据组件，存储在胸甲的 ItemStack 中。
 * 封装了原鞘翅的所有数据，支持序列化、网络同步。
 */
public record ElytraComponent(
        // 来源模组ID，如 "twilightforest"
        ResourceLocation sourceNamespace,
        // 原鞘翅物品ID
        ResourceLocation originalElytraId,
        // 原鞘翅的所有 DataComponent（用于拆卸时100%还原）
        DataComponentMap originalElytraComponents,
        // 当前耐久（剩余飞行耐久）
        int currentDurability,
        // 最大耐久
        int maxDurability,
        // 可选：覆盖纹理路径
        @Nullable ResourceLocation textureOverride,
        // 可选：额外 NBT 数据（供未来扩展）
        @Nullable CompoundTag extraData
) {

    /**
     * Codec 用于持久化存储（物品保存到箱子/末影箱/切换维度时）
     */
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
                            .forGetter(c -> Optional.ofNullable(c.extraData))
            ).apply(instance, (sourceNamespace, originalElytraId, originalElytraComponents,
                               currentDurability, maxDurability,
                               textureOverrideOpt, extraDataOpt) ->
                    new ElytraComponent(
                            sourceNamespace,
                            originalElytraId,
                            originalElytraComponents,
                            currentDurability,
                            maxDurability,
                            textureOverrideOpt.orElse(null),
                            extraDataOpt.orElse(null)
                    )
            )
    );

    /**
     * StreamCodec 用于网络同步（服务端 → 客户端）
     * 使用手动编码避免 composite() 字段数量限制
     */
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

            return new ElytraComponent(
                    sourceNamespace,
                    originalElytraId,
                    originalElytraComponents,
                    currentDurability,
                    maxDurability,
                    textureOverrideOpt.orElse(null),
                    extraDataOpt.orElse(null)
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
        }
    };
}

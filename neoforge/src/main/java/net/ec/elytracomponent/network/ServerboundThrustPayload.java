package net.ec.elytracomponent.network;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 客户端 → 服务端：jump_thrust 推进心跳包，无数据体。
 * 服务端收到后刷新 ElytraThrustTracker，用于在正确时机扣耐久/发粒子。
 */
public record ServerboundThrustPayload() implements CustomPacketPayload {

    public static final ServerboundThrustPayload INSTANCE = new ServerboundThrustPayload();

    public static final Type<ServerboundThrustPayload> TYPE =
            new Type<>(ElytraComponentMod.id("thrust"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThrustPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package net.ec.elytracomponent.network;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.ability.ElytraThrustTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：jump_thrust 推进心跳包，无数据体。
 * 服务端收到后刷新 ElytraThrustTracker，用于在正确时机扣耐久/发粒子。
 */
public class ThrustMessage {

    public ThrustMessage() {}

    public static void encode(ThrustMessage msg, FriendlyByteBuf buf) {}

    public static ThrustMessage decode(FriendlyByteBuf buf) {
        return new ThrustMessage();
    }

    public static void handle(ThrustMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
                ElytraThrustTracker.mark(ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}

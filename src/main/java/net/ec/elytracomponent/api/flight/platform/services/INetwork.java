package net.ec.elytracomponent.api.flight.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface INetwork {
    <TMessage extends CustomPacketPayload> ServerMessageBus<TMessage> clientToServer(
            CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, TMessage> type,
            OnServerMessage<TMessage> handler
    );

    <TMessage extends CustomPacketPayload> ClientMessageBus<TMessage> serverToClient(
            CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, TMessage> type,
            OnClientMessage<TMessage> handler
    );

    @FunctionalInterface
    interface OnServerMessage<TMessage> {
        void handle(TMessage message, ServerPlayer player);
    }

    @FunctionalInterface
    interface OnClientMessage<TMessage> {
        void handle(TMessage message, Player player);
    }
}

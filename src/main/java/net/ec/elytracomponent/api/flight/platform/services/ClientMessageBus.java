package net.ec.elytracomponent.api.flight.platform.services;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ClientMessageBus<TMessage> {
    void send(ServerPlayer player, TMessage message);
}

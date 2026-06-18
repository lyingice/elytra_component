package net.ec.elytracomponent.api.flight.platform.services;

@FunctionalInterface
public interface ServerMessageBus<TMessage> {
    void send(TMessage message);
}

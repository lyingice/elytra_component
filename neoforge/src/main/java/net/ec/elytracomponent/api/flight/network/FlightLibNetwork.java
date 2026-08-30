package net.ec.elytracomponent.api.flight.network;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.server.level.ServerPlayer;

/**
 * Network registration for flight lib packets.
 * Uses the existing ElytraComponentMod.addNetworkMessage() system.
 */
public class FlightLibNetwork {

    public static void register() {
        // KeyPressedEvent: client -> server
        ElytraComponentMod.addNetworkMessage(
                KeyPressedEvent.TYPE,
                KeyPressedEvent.STREAM_CODEC,
                (message, context) -> {
                    var player = context.player();
                    if (player instanceof ServerPlayer serverPlayer) {
                        message.handle(serverPlayer);
                    }
                }
        );

        // KeysSyncEvent: server -> client
        ElytraComponentMod.addNetworkMessage(
                KeysSyncEvent.TYPE,
                KeysSyncEvent.STREAM_CODEC,
                (message, context) -> {
                    var player = context.player();
                    message.handle(player);
                }
        );
    }
}

package net.ec.elytracomponent.api.flight.logic;

import net.ec.elytracomponent.api.flight.FlightKey;
import net.ec.elytracomponent.api.flight.IFlightApi;
import net.ec.elytracomponent.api.flight.network.KeyPressedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class ControlSender {
    private static final Map<FlightKey, Long> lastPresses = new HashMap<>();

    private static boolean canPressAgain(FlightKey key) {
        var last = lastPresses.get(key);
        if (last == null) return true;
        return (System.currentTimeMillis() - last) > 150;
    }

    private static void send(KeyPressedEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        PacketDistributor.sendToServer(event);
        ControlManager.setKey(player, event.getKey(), event.isPressed());
    }

    public static void checkKeys() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var jetpack = IFlightApi.INSTANCE.findJetpack(player);
        if (jetpack == null) return;

        for (var key : FlightKey.values()) {
            if (!key.toggle) continue;
            var binding = key.binding;
            if (binding.isEmpty()) continue;
            if (!binding.get().isDown()) continue;
            if (!canPressAgain(key)) continue;

            lastPresses.put(key, System.currentTimeMillis());
            boolean newState = !key.isPressed(player);
            send(new KeyPressedEvent(key, newState, true));
        }
    }

    public static void onTick(LocalPlayer player) {
        // Send non-toggle keys
        for (var key : FlightKey.values()) {
            if (key.toggle) continue;
            if (key.binding.isEmpty()) continue;
            send(new KeyPressedEvent(key, key.binding.get().isDown()));
        }

        // Vanilla controls
        send(new KeyPressedEvent(FlightKey.UP, player.input.jumping));
        send(new KeyPressedEvent(FlightKey.LEFT, player.input.left));
        send(new KeyPressedEvent(FlightKey.RIGHT, player.input.right));
        send(new KeyPressedEvent(FlightKey.FORWARD, player.input.forwardImpulse > 0));
        send(new KeyPressedEvent(FlightKey.BACKWARD, player.input.forwardImpulse < 0));
    }
}

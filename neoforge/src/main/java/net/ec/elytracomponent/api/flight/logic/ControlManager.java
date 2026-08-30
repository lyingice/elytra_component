package net.ec.elytracomponent.api.flight.logic;

import com.mojang.blaze3d.platform.InputConstants;
import net.ec.elytracomponent.api.flight.FlightKey;
import net.ec.elytracomponent.api.flight.network.KeysSyncEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Consumer;

public class ControlManager {

    public static boolean isPressed(FlightKey key, LivingEntity entity) {
        if (entity instanceof ISettingsStorage storage) {
            return ISettingsStorage.isPressed(storage, key);
        }
        return false;
    }

    public static void setKey(LivingEntity entity, FlightKey key, boolean pressed) {
        if (entity instanceof ISettingsStorage storage) {
            ISettingsStorage.setKey(storage, key, pressed);
        }
    }

    public static void registerKeybinds(Consumer<KeyMapping> registry) {
        for (var key : FlightKey.values()) {
            key.binding = Optional.ofNullable(key.defaultKey).map(defaultKey -> {
                var mapping = new KeyMapping(
                        "key.jetpack." + key.name().toLowerCase() + ".description",
                        InputConstants.Type.KEYSYM,
                        defaultKey,
                        "key.categories.elytra_component.jetpack"
                );
                registry.accept(mapping);
                return mapping;
            });
        }
    }

    public static void load(ServerPlayer player) {
        if (player instanceof ISettingsStorage storage) {
            var keys = storage.flightlib$get();
            if (keys == null) {
                keys = ISettingsStorage.defaultSettings();
            }
            var event = new KeysSyncEvent(keys);
            PacketDistributor.sendToPlayer(player, event);
        }
    }
}

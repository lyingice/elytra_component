package net.ec.elytracomponent.api.flight.logic;

import net.ec.elytracomponent.api.flight.FlightKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public interface ISettingsStorage {
    void flightlib$set(Map<FlightKey, Boolean> settings);
    Map<FlightKey, Boolean> flightlib$get();

    static Map<FlightKey, Boolean> defaultSettings() {
        Map<FlightKey, Boolean> defaults = new HashMap<>();
        defaults.put(FlightKey.TOGGLE_ACTIVE, true);
        defaults.put(FlightKey.TOGGLE_HOVER, true);
        return defaults;
    }

    StreamCodec<FriendlyByteBuf, Map<FlightKey, Boolean>> KEYS_STREAM_CODEC = StreamCodec.of(
            (buf, keys) -> {
                buf.writeMap(keys, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeBoolean);
            },
            (buf) -> {
                return buf.readMap(
                        (b) -> b.readEnum(FlightKey.class),
                        FriendlyByteBuf::readBoolean
                );
            }
    );

    static boolean isPressed(ISettingsStorage storage, FlightKey key) {
        return storage.flightlib$get().getOrDefault(key, key.getDefault());
    }

    static void setKey(ISettingsStorage storage, FlightKey key, boolean pressed) {
        var keys = new HashMap<>(storage.flightlib$get());
        keys.put(key, pressed);
        storage.flightlib$set(keys);
    }

    // Apply default settings
    static void applyDefaults(ISettingsStorage storage) {
        var current = storage.flightlib$get();
        if (current == null || current.isEmpty()) {
            storage.flightlib$set(defaultSettings());
        }
    }
}

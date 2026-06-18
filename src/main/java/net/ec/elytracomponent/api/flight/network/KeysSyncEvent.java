package net.ec.elytracomponent.api.flight.network;

import net.ec.elytracomponent.api.flight.Constants;
import net.ec.elytracomponent.api.flight.FlightKey;
import net.ec.elytracomponent.api.flight.logic.ControlManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class KeysSyncEvent implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KeysSyncEvent> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "keys_sync"));

    public static final StreamCodec<FriendlyByteBuf, KeysSyncEvent> STREAM_CODEC = StreamCodec.of(
            KeysSyncEvent::encode,
            KeysSyncEvent::decode
    );

    private final Map<FlightKey, Boolean> settings;

    public KeysSyncEvent(Map<FlightKey, Boolean> settings) {
        this.settings = settings;
    }

    public Map<FlightKey, Boolean> getSettings() {
        return settings;
    }

    private static void encode(FriendlyByteBuf buf, KeysSyncEvent event) {
        buf.writeMap(event.settings, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeBoolean);
    }

    private static KeysSyncEvent decode(FriendlyByteBuf buf) {
        var settings = buf.readMap(
                b -> b.readEnum(FlightKey.class),
                FriendlyByteBuf::readBoolean
        );
        return new KeysSyncEvent(settings);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        for (var entry : settings.entrySet()) {
            ControlManager.setKey(player, entry.getKey(), entry.getValue());
        }
    }
}

package net.ec.elytracomponent.api.flight.network;

import net.ec.elytracomponent.api.flight.Constants;
import net.ec.elytracomponent.api.flight.FlightKey;
import net.ec.elytracomponent.api.flight.logic.ControlManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class KeyPressedEvent implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KeyPressedEvent> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "key_pressed"));

    public static final StreamCodec<FriendlyByteBuf, KeyPressedEvent> STREAM_CODEC = StreamCodec.of(
            (buf, event) -> {
                buf.writeEnum(event.key);
                buf.writeBoolean(event.pressed);
                buf.writeBoolean(event.notify);
            },
            (buf) -> {
                var key = buf.readEnum(FlightKey.class);
                return new KeyPressedEvent(key, buf.readBoolean(), buf.readBoolean());
            }
    );

    private final FlightKey key;
    private final boolean pressed;
    private final boolean notify;

    public KeyPressedEvent(FlightKey key, boolean pressed, boolean notify) {
        this.key = key;
        this.pressed = pressed;
        this.notify = notify;
    }

    public KeyPressedEvent(FlightKey key, boolean pressed) {
        this(key, pressed, false);
    }

    public FlightKey getKey() { return key; }
    public boolean isPressed() { return pressed; }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        if (notify) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message." + Constants.MOD_ID + ".control." + key.name().toLowerCase(),
                            Component.translatable(
                                    "message." + Constants.MOD_ID + ".control." + (pressed ? "on" : "off")
                            )
                    ),
                    true
            );
        }
        ControlManager.setKey(player, key, pressed);
    }
}

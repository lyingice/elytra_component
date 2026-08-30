package net.ec.elytracomponent.ability;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 记录客户端 jump_thrust 推进心跳：客户端每次推进时发一个
 * ServerboundThrustPayload，服务端标记后 3 game tick 内视为活跃。
 * 用 WeakHashMap 以 ServerPlayer 为键，玩家退出自动清理。
 */
public final class ElytraThrustTracker {

    private static final Map<ServerPlayer, Long> LAST_THRUST = new WeakHashMap<>();
    private static final int ACTIVE_WINDOW_TICKS = 3;

    public static void mark(ServerPlayer player) {
        LAST_THRUST.put(player, player.serverLevel().getGameTime());
    }

    public static boolean isActive(ServerPlayer player) {
        Long last = LAST_THRUST.get(player);
        if (last == null) return false;
        long now = player.serverLevel().getGameTime();
        if (now - last > ACTIVE_WINDOW_TICKS) {
            LAST_THRUST.remove(player);
            return false;
        }
        return true;
    }

    private ElytraThrustTracker() {}
}

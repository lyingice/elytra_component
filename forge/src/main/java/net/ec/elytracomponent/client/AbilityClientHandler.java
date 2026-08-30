package net.ec.elytracomponent.client;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.ability.JumpThrustAbility;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.network.ThrustMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * jump_thrust 客户端推进：滑翔中按住跳跃键，按视线方向施加推力。
 * 数据包定义的 abilities 参数驱动，参数与 mut 参考实现一致：
 *   thrust（1.5）、smooth_factor（0.5）、turn_sensitivity（0.1）、cooldown_ticks（5）。
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID, value = Dist.CLIENT)
public final class AbilityClientHandler {

    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.CLIENT) return;
        Player player = event.player;

        if (!player.isFallFlying()) {
            COOLDOWNS.remove(player.getUUID());
            return;
        }

        CompoundTag config = findJumpThrust(player);
        if (config == null) return;

        int cooldown = COOLDOWNS.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            COOLDOWNS.put(player.getUUID(), cooldown - 1);
            return;
        }
        if (!Minecraft.getInstance().options.keyJump.isDown()) return;
        COOLDOWNS.put(player.getUUID(), Math.max(1, config.contains("cooldown_ticks")
                ? config.getInt("cooldown_ticks") : 5));

        applyThrust(player, config);
        ElytraComponentMod.PACKET_HANDLER.sendToServer(new ThrustMessage());
    }

    private static void applyThrust(Player player, CompoundTag config) {
        double thrust = config.contains("thrust") ? config.getDouble("thrust") : 1.5;
        double smooth = config.contains("smooth_factor") ? config.getDouble("smooth_factor") : 0.5;
        double turn = config.contains("turn_sensitivity") ? config.getDouble("turn_sensitivity") : 0.1;

        Vec3 look = player.getLookAngle();
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.add(
                look.x * turn + (look.x * thrust - velocity.x) * smooth,
                look.y * turn + (look.y * thrust - velocity.y) * smooth,
                look.z * turn + (look.z * thrust - velocity.z) * smooth));
        player.hasImpulse = true;
    }

    // ==================== 查找 jump_thrust 配置 ====================

    private static CompoundTag findJumpThrust(Player player) {
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = ModComponents.getComponent(chest);
        if (component == null) return null;
        return JumpThrustAbility.getConfig(component);
    }

    private AbilityClientHandler() {}
}

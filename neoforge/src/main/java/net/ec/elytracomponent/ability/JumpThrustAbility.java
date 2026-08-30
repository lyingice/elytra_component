package net.ec.elytracomponent.ability;

import net.ec.elytracomponent.Config;
import net.ec.elytracomponent.api.ability.IElytraAbility;
import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * jump_thrust：滑翔中按住跳跃键获得沿视线方向的喷气推进。
 * 数据包 abilities 参数（均可选）：
 *   thrust（默认 1.5）、smooth_factor（0.5）、turn_sensitivity（0.1）、
 *   cooldown_ticks（5）、particle（默认 minecraft:flame）、
 *   durability_per_thrust（默认 1，受胸甲耐久附魔与 Config.consumptionRate 影响）
 *
 * 客户端负责施加速度（见 client.AbilityClientHandler），每帧推进时向服务端
 * 发送 ServerboundThrustPayload；服务端在本方法内扣除耐久并生成尾焰粒子。
 */
public class JumpThrustAbility implements IElytraAbility {

    public static final String ID = "jump_thrust";

    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    @Override
    public String getAbilityId() {
        return ID;
    }

    @Override
    public void onEquip(Player player, ItemStack chestplate, ElytraComponent component) {
    }

    @Override
    public void onUnequip(Player player, ItemStack chestplate, ElytraComponent component) {
        COOLDOWNS.remove(player.getUUID());
    }

    @Override
    public boolean onFlightTick(Player player, ItemStack chestplate, ElytraComponent component) {
        if (!(player instanceof ServerPlayer serverPlayer)) return true;
        CompoundTag config = getConfig(component);
        if (config == null) return true;

        // 客户端未在推进（未按跳跃）时不消耗耐久
        if (!ElytraThrustTracker.isActive(serverPlayer)) return true;

        java.util.UUID uuid = player.getUUID();
        int cooldown = COOLDOWNS.getOrDefault(uuid, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(uuid, cooldown - 1);
            return true;
        }
        COOLDOWNS.put(uuid, Math.max(1, config.contains("cooldown_ticks")
                ? config.getInt("cooldown_ticks") : 5));

        consumeDurability(serverPlayer, chestplate, component, config);
        spawnThrustParticles(serverPlayer, config);
        return true;
    }

    // ==================== 耐久消耗 ====================

    private void consumeDurability(ServerPlayer player, ItemStack chestplate, ElytraComponent component, CompoundTag config) {
        int max = component.maxDurability();
        if (max <= 0) return; // 无耐久限制的鞘翅

        int base = config.contains("durability_per_thrust") ? Math.max(0, config.getInt("durability_per_thrust")) : 1;
        int amount = Math.round(base * (float) Config.consumptionRate());
        if (amount <= 0) return;

        // 胸甲的耐久附魔同样保护组件耐久：等级/(等级+1) 概率免除
        var enchantments = player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        int unbreaking = chestplate.getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING));
        if (unbreaking > 0 && player.getRandom().nextInt(unbreaking + 1) != 0) {
            amount = 0;
        }
        if (amount <= 0) return;

        int newDurability = Math.max(0, component.currentDurability() - amount);
        chestplate.set(net.ec.elytracomponent.component.ModComponents.ELYTRA_COMPONENT.get(),
                component.withDurability(newDurability));
    }

    // ==================== 尾焰粒子 ====================

    private void spawnThrustParticles(ServerPlayer player, CompoundTag config) {
        if (!(player.serverLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        Vec3 look = player.getLookAngle();
        SimpleParticleType particle = ParticleTypes.FLAME;
        if (config.contains("particle")) {
            var particleHolder = BuiltInRegistries.PARTICLE_TYPE.get(
                    ResourceLocation.parse(config.getString("particle")));
            if (particleHolder instanceof SimpleParticleType simple) particle = simple;
        }
        for (int i = 0; i < 5; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.5;
            double offsetY = (player.getRandom().nextDouble() - 0.5) * 0.5;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.5;
            level.sendParticles(particle,
                    player.getX() + offsetX,
                    player.getY() + 0.5 + offsetY,
                    player.getZ() + offsetZ,
                    1,
                    -look.x * 0.1, -look.y * 0.1, -look.z * 0.1, 0.01);
        }
    }

    // ==================== 配置读取 ====================

    /** 取组件里 jump_thrust 的参数段（兼容顶层扁平与 abilities 列表两种格式） */
    @SuppressWarnings("unused")
    public static CompoundTag getConfig(ElytraComponent component) {
        CompoundTag config = component.abilityConfig();
        if (config == null) return null;
        if (ID.equals(config.getString("type"))) return config;
        if (config.contains("abilities", CompoundTag.TAG_LIST)) {
            var list = config.getList("abilities", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (ID.equals(entry.getString("type"))) return entry;
            }
        }
        return null;
    }

    @Override
    public void addTooltip(ItemStack stack, ElytraComponent component, List<Component> tooltip) {
        tooltip.add(Component.translatable("ability.elytra_component.jump_thrust"));
    }
}

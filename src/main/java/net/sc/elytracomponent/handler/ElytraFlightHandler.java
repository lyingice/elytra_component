package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.sc.elytracomponent.api.ability.IElytraAbility;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraFlightHandler {

    // ==================== 可自定义：飞行条件 ====================

    public static boolean canHandleFlight(LivingEntity living) {
        if (living.level().isClientSide) return false;
        if (living instanceof Player player && player.getAbilities().instabuild) return false;
        if (living.isSpectator()) return false;
        return true;
    }

    public static boolean hasComponent(LivingEntity living) {
        ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
        return ModComponents.hasComponent(chest);
    }

    // ==================== 事件 ====================

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Player player = event.player;
        if (!canHandleFlight(player)) return;
        if (!hasComponent(player)) return;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = ModComponents.getComponent(chestStack);
        if (component == null) return;

        // 能力接管
        if (onAbilityTick(player, chestStack, component)) return;

        // 默认鞘翅逻辑（必须在飞行中）
        if (!player.isFallFlying()) return;
        onDefaultFlightTick(player, chestStack, component);
    }

    // ==================== 可自定义：能力接管 ====================

    public static boolean onAbilityTick(Player player, ItemStack chestStack, ElytraComponent component) {
        CompoundTag abilityConfig = component.abilityConfig();
        if (abilityConfig == null || !abilityConfig.contains("type")) return false;

        String abilityId = abilityConfig.getString("type");
        IElytraAbility ability = ElytraAbilityRegistry.create(abilityId);
        if (ability == null) return false;

        return ability.onFlightTick(player, chestStack, component);
    }

    // ==================== 可自定义：默认耐久消耗 ====================

    public static void onDefaultFlightTick(Player player, ItemStack chestStack, ElytraComponent component) {
        if (player.tickCount % 10 != 0) return;

        int newDurability = component.currentDurability() - 1;
        if (newDurability <= 0) {
            onDurabilityDepleted(player, chestStack, component);
        } else {
            onDurabilityConsume(player, chestStack, component, newDurability);
        }
    }

    public static void onDurabilityDepleted(Player player, ItemStack chestStack, ElytraComponent component) {
        ElytraComponent broken = new ElytraComponent(
                component.sourceNamespace(), component.originalElytraId(),
                component.originalElytraTag(), 0, component.maxDurability(),
                component.textureOverride(), component.extraData(),
                component.originalChestAttributes(), component.abilityConfig(),
                component.particleConfig()
        );
        ModComponents.setComponent(chestStack, broken);
    }

    public static void onDurabilityConsume(Player player, ItemStack chestStack, ElytraComponent component, int newDurability) {
        ElytraComponent updated = new ElytraComponent(
                component.sourceNamespace(), component.originalElytraId(),
                component.originalElytraTag(), newDurability, component.maxDurability(),
                component.textureOverride(), component.extraData(),
                component.originalChestAttributes(), component.abilityConfig(),
                component.particleConfig()
        );
        ModComponents.setComponent(chestStack, updated);

        if (player.tickCount % 20 == 0) {
            player.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
        }
    }
}

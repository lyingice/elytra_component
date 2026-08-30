package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.ec.elytracomponent.api.ability.IElytraAbility;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

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
        return chest.has(ModComponents.ELYTRA_COMPONENT.get());
    }

    // ==================== 事件 ====================

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!canHandleFlight(living)) return;
        if (!hasComponent(living)) return;

        ItemStack chestStack = living.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = chestStack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        // 能力接管
        if (onAbilityTick(living, chestStack, component)) return;

        // 默认鞘翅逻辑（必须在飞行中）
        if (!living.isFallFlying()) return;
        onDefaultFlightTick(living, chestStack, component);
    }

    // ==================== 可自定义：能力接管 ====================

    public static boolean onAbilityTick(LivingEntity living, ItemStack chestStack, ElytraComponent component) {
        CompoundTag abilityConfig = component.abilityConfig();
        if (abilityConfig == null || !abilityConfig.contains("type")) return false;

        String abilityId = abilityConfig.getString("type");
        IElytraAbility ability = ElytraAbilityRegistry.create(abilityId);
        if (ability == null) return false;

        return ability.onFlightTick((Player) living, chestStack, component);
    }

    // ==================== 可自定义：默认耐久消耗 ====================

    public static void onDefaultFlightTick(LivingEntity living, ItemStack chestStack, ElytraComponent component) {
        // maxDurability <= 0：无耐久限制的鞘翅，自由飞行不扣耐久
        if (component.maxDurability() <= 0) return;

        if (living.tickCount % 10 != 0) return;

        // 胸甲的耐久附魔同样保护组件耐久：等级/(等级+1) 概率免除本次损耗
        var enchantments = living.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        int unbreaking = chestStack.getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING));
        if (unbreaking > 0 && living.getRandom().nextInt(unbreaking + 1) != 0) return;

        int newDurability = component.currentDurability() - 1;
        if (newDurability <= 0) {
            onDurabilityDepleted(living, chestStack, component);
        } else {
            onDurabilityConsume(living, chestStack, component, newDurability);
        }
    }

    public static void onDurabilityDepleted(LivingEntity living, ItemStack chestStack, ElytraComponent component) {
        ElytraComponent broken = new ElytraComponent(new ElytraComponent.SourceInfo(component.source().sourceNamespace(), component.source().originalElytraId(), component.source().originalElytraTag()), 0, component.maxDurability(), component.textureOverride(), component.extraData(), component.originalChestAttributes(), component.abilityConfig(), component.particleConfig());
        chestStack.set(ModComponents.ELYTRA_COMPONENT.get(), broken);
    }

    public static void onDurabilityConsume(LivingEntity living, ItemStack chestStack, ElytraComponent component, int newDurability) {
        ElytraComponent updated = new ElytraComponent(new ElytraComponent.SourceInfo(component.source().sourceNamespace(), component.source().originalElytraId(), component.source().originalElytraTag()), newDurability, component.maxDurability(), component.textureOverride(), component.extraData(), component.originalChestAttributes(), component.abilityConfig(), component.particleConfig());
        chestStack.set(ModComponents.ELYTRA_COMPONENT.get(), updated);

        if (living.tickCount % 20 == 0) {
            living.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
        }
    }
}

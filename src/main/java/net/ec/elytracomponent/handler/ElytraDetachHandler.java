package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.ec.elytracomponent.api.ability.IElytraAbility;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraDetachHandler {

    /** 检查物品是否可以拆卸。附属模组可覆盖此逻辑 */
    public static boolean canDetach(ItemStack stack) {
        return stack.has(ModComponents.ELYTRA_COMPONENT.get());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack offHand = player.getOffhandItem();

        if (!canDetach(offHand)) return;

        ElytraComponent component = offHand.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component != null && component.abilityConfig() != null
                && component.abilityConfig().contains("type")) {
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        detachElytra(player, level, offHand, InteractionHand.OFF_HAND);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void detachElytra(Player player, Level level, ItemStack sourceStack, InteractionHand hand) {
        ElytraComponent component = sourceStack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        onBeforeDetach(player, sourceStack, component);
        onRestoreAttributes(sourceStack, component);

        ItemStack restored = onRestoreItem(player, component);
        onRemoveComponent(sourceStack);
        onGiveItem(player, restored);

        player.setItemInHand(hand, sourceStack);
        onAfterDetach(player, level, restored);
    }

    public static void onBeforeDetach(Player player, ItemStack sourceStack, ElytraComponent component) {
        CompoundTag abilityConfig = component.abilityConfig();
        if (abilityConfig != null && abilityConfig.contains("type")) {
            IElytraAbility ability = ElytraAbilityRegistry.create(abilityConfig.getString("type"));
            if (ability != null) ability.onUnequip(player, sourceStack, component);
        }
    }

    public static void onRestoreAttributes(ItemStack sourceStack, ElytraComponent component) {
        restoreOriginalAttributes(sourceStack, component);
    }

    public static ItemStack onRestoreItem(Player player, ElytraComponent component) {
        return ElytraComponentAPI.restoreElytra(component);
    }

    public static void onRemoveComponent(ItemStack sourceStack) {
        ElytraComponentAPI.removeComponent(sourceStack);
    }

    public static void onGiveItem(Player player, ItemStack restored) {
        if (restored.isEmpty()) return;
        ItemStack copy = restored.copy();
        boolean given = player.getInventory().add(copy);
        if (!given) player.drop(copy, false);
    }

    public static void onAfterDetach(Player player, Level level, ItemStack restored) {
        if (restored.isEmpty()) return;
        net.minecraft.network.chat.Component name = restored.getHoverName();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1);
        }
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.elytra_detached", name), true);
    }

    // ==================== 属性恢复 ====================

    private static void restoreOriginalAttributes(ItemStack chestplate, ElytraComponent component) {
        CompoundTag origAttrs = component.originalChestAttributes();
        if (origAttrs != null && origAttrs.contains("size")) {
            int size = origAttrs.getInt("size");
            List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                CompoundTag entryTag = origAttrs.getCompound("modifier_" + i);
                if (!entryTag.isEmpty()) {
                    var attr = BuiltInRegistries.ATTRIBUTE.get(
                            ResourceLocation.parse(entryTag.getString("attribute")));
                    if (attr != null) {
                        AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(
                                entryTag.getString("operation"));
                        EquipmentSlot slot = EquipmentSlot.valueOf(entryTag.getString("slot"));
                        entries.add(new ItemAttributeModifiers.Entry(
                                BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr),
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("elytra_component_restored"),
                                        entryTag.getDouble("amount"), op),
                                EquipmentSlotGroup.bySlot(slot)));
                    }
                }
            }
            if (!entries.isEmpty()) {
                chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS,
                        new ItemAttributeModifiers(entries, true));
            } else {
                chestplate.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            }
        } else {
            chestplate.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        }
    }
}
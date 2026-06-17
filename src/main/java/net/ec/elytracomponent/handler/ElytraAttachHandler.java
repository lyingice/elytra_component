package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraAttachHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (tryAttach(event.getEntity(), event.getLevel())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (tryAttach(event.getEntity(), event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (tryAttach(event.getEntity(), event.getLevel())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static boolean tryAttach(Player player, Level level) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!ElytraComponentReloadListener.isRegisteredElytra(mainHand.getItem())) return false;
        if (!offHand.is(Items.SLIME_BALL)) return false;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return false;

        if (level.isClientSide) return true;

        ElytraComponent existing = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
        if (existing != null && existing.currentDurability() > 0) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.elytra_component.already_has_component",
                            chestplate.getHoverName()), true);
            return true;
        }

        ElytraComponentDefinition def = ElytraComponentReloadListener.findByItem(mainHand.getItem());
        if (def == null) return false;

        // ========== 保存原始胸甲属性 ==========
        CompoundTag originalChestAttrs = null;
        ItemAttributeModifiers chestModifiers = chestplate.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (chestModifiers != null) {
            originalChestAttrs = new CompoundTag();
            var modifiers = chestModifiers.modifiers();
            originalChestAttrs.putInt("size", modifiers.size());
            for (int i = 0; i < modifiers.size(); i++) {
                var entry = modifiers.get(i);
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("attribute", entry.attribute().getRegisteredName());
                entryTag.putDouble("amount", entry.modifier().amount());
                entryTag.putString("operation", entry.modifier().operation().name());
                entryTag.putString("slot", entry.slot().name());
                originalChestAttrs.put("modifier_" + i, entryTag);
            }
        }

        // ========== 合并鞘翅属性到胸甲 ==========
        ItemAttributeModifiers elytraModifiers = mainHand.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (elytraModifiers != null && !elytraModifiers.modifiers().isEmpty()) {
            chestplate.set(DataComponents.ATTRIBUTE_MODIFIERS,
                    mergeAttributeModifiers(chestModifiers, elytraModifiers));
        }

        // ========== 提取附魔 ==========
        ItemEnchantments enchantments = mainHand.get(DataComponents.ENCHANTMENTS);

        // ========== 创建组件 ==========
        net.minecraft.network.chat.Component chestplateName = chestplate.getHoverName();
        net.minecraft.network.chat.Component elytraName = mainHand.getHoverName();

        ElytraComponent component = ElytraComponentAPI.createComponent(mainHand, def, originalChestAttrs);

        ElytraComponentAPI.setComponent(chestplate, component);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        // 消耗鞘翅
        mainHand.shrink(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);

        // 返还附魔书
        if (enchantments != null && !enchantments.isEmpty()) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
            if (!player.getInventory().add(enchantedBook)) {
                player.drop(enchantedBook, false);
            }
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.elytra_component.enchants_returned"), true);
        }

        if (!player.getAbilities().instabuild) {
            offHand.shrink(1);
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);
        }

        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.elytra_attached",
                        chestplateName, elytraName), true);

        return true;
    }

    private static ItemAttributeModifiers mergeAttributeModifiers(
            @Nullable ItemAttributeModifiers base,
            ItemAttributeModifiers addition) {
        if (base == null) return addition;
        List<ItemAttributeModifiers.Entry> merged = new ArrayList<>(base.modifiers());
        merged.addAll(addition.modifiers());
        return new ItemAttributeModifiers(merged, true);
    }
}
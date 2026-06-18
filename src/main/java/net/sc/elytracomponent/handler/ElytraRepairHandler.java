package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ElytraRepairAPI;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraRepairHandler {

    // ==================== 事件 ====================

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        if (!ElytraRepairAPI.canRepair(mainHand)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = ModComponents.getComponent(chestplate);
        if (component == null) return;

        // 有能力接管？跳过默认修复
        if (ElytraRepairAPI.skipDefaultRepair(component)) return;

        if (!ElytraRepairAPI.needsRepair(component)) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()), true);
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        onRepair(player, level, chestplate, component, mainHand);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        if (!ElytraRepairAPI.canRepair(mainHand)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = ModComponents.getComponent(chestplate);
        if (component == null) return;

        if (ElytraRepairAPI.skipDefaultRepair(component)) return;

        if (!ElytraRepairAPI.needsRepair(component)) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()), true);
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        onRepair(player, level, chestplate, component, mainHand);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        if (!ElytraRepairAPI.canRepair(mainHand)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = ModComponents.getComponent(chestplate);
        if (component == null) return;

        if (ElytraRepairAPI.skipDefaultRepair(component)) return;

        if (!ElytraRepairAPI.needsRepair(component)) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()), true);
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        onRepair(player, level, chestplate, component, mainHand);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ==================== 可自定义：修复逻辑 ====================

    public static void onRepair(Player player, Level level, ItemStack chestplate,
                                ElytraComponent component, ItemStack repairItem) {
        int newDurability = component.currentDurability() + ElytraRepairAPI.getRepairAmount(component);

        ElytraComponent repaired = createRepairedComponent(component, newDurability);
        ModComponents.setComponent(chestplate, repaired);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        onRepairConsume(player, repairItem);
        onRepairEffects(level, player);
        onRepairMessage(player, chestplate, newDurability, component.maxDurability());

        ElytraRepairAPI.onRepaired(player, chestplate, component);
    }

    public static ElytraComponent createRepairedComponent(ElytraComponent component, int newDurability) {
        return new ElytraComponent(
                component.sourceNamespace(), component.originalElytraId(),
                component.originalElytraTag(), newDurability, component.maxDurability(),
                component.textureOverride(), component.extraData(),
                component.originalChestAttributes(), component.abilityConfig(),
                component.particleConfig()
        );
    }

    public static void onRepairConsume(Player player, ItemStack repairItem) {
        if (!player.getAbilities().instabuild) {
            repairItem.shrink(1);
        }
    }

    public static void onRepairEffects(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 1.5F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    8, 0.3, 0.3, 0.3, 0.02);
        }
    }

    public static void onRepairMessage(Player player, ItemStack chestplate, int newDurability, int maxDurability) {
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.repair_success",
                        chestplate.getHoverName(), newDurability, maxDurability), true);
    }
}

package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraRepairHandler {

    /**
     * 每张幻翼膜恢复的耐久值
     */
    private static final int REPAIR_AMOUNT = 108;

    // ==================== 右键空气/物品 ====================
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        // 主手必须是幻翼膜
        if (!mainHand.is(Items.PHANTOM_MEMBRANE)) return;

        // 身上必须穿着带鞘翅组件的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        // 检查是否需要修复
        if (component.currentDurability() >= component.maxDurability()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()),
                        true
                );
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // 执行修复
        repairComponent(player, level, chestplate, component, mainHand);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ==================== 右键实体 ====================
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        if (!mainHand.is(Items.PHANTOM_MEMBRANE)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        if (component.currentDurability() >= component.maxDurability()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()),
                        true
                );
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        repairComponent(player, level, chestplate, component, mainHand);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ==================== 右键方块 ====================
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack mainHand = player.getMainHandItem();

        if (!mainHand.is(Items.PHANTOM_MEMBRANE)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ElytraComponent component = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        if (component.currentDurability() >= component.maxDurability()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.elytra_component.repair_not_needed",
                                chestplate.getHoverName()),
                        true
                );
            }
            return;
        }

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        repairComponent(player, level, chestplate, component, mainHand);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ==================== 内部方法 ====================

    /**
     * 执行修复操作
     */
    private static void repairComponent(Player player, Level level,
                                        ItemStack chestplate, ElytraComponent component,
                                        ItemStack membrane) {
        int currentDurability = component.currentDurability();
        int maxDurability = component.maxDurability();
        int newDurability = Math.min(currentDurability + REPAIR_AMOUNT, maxDurability);

        // 创建修复后的组件
        ElytraComponent repairedComponent = new ElytraComponent(
                component.sourceNamespace(),
                component.originalElytraId(),
                component.originalElytraComponents(),
                newDurability,
                component.maxDurability(),
                component.textureOverride(),
                component.extraData(),
                component.originalChestAttributes(),
                component.abilityConfig(),
                component.particleConfig()
        );

        // 设置修复后的组件
        chestplate.set(ModComponents.ELYTRA_COMPONENT.get(), repairedComponent);

        // 更新身上的胸甲
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        // 消耗幻翼膜
        if (!player.getAbilities().instabuild) {
            membrane.shrink(1);
        }

        // 音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 1.5F);

        // 粒子效果
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    8, 0.3, 0.3, 0.3, 0.02
            );
        }

        // 提示
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.repair_success",
                        chestplate.getHoverName(),
                        newDurability,
                        maxDurability),
                true
        );
    }
}
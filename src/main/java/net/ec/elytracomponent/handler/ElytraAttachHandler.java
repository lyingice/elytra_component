package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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

        // 主手：鞘翅物品
        if (!ElytraComponentReloadListener.isRegisteredElytra(mainHand.getItem())) return false;

        // 副手：粘液球
        if (!offHand.is(Items.SLIME_BALL)) return false;

        // 身上：胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return false;

        if (level.isClientSide) {
            return true;
        }

        // 检查已有组件
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

        // 保存名称
        net.minecraft.network.chat.Component chestplateName = chestplate.getHoverName();
        net.minecraft.network.chat.Component elytraName = mainHand.getHoverName();

        // 创建组件并设置
        ElytraComponent component = ElytraComponentAPI.createComponent(mainHand, def);
        ElytraComponentAPI.setComponent(chestplate, component);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        // 消耗主手鞘翅
        mainHand.shrink(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);

        // 消耗副手粘液球
        if (!player.getAbilities().instabuild) {
            offHand.shrink(1);
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        }

        // 音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8F, 1.2F);

        // 粒子
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);
        }

        // 提示
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.elytra_attached",
                        chestplateName, elytraName), true);

        return true;
    }
}
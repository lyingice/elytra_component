package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ElytraComponentAPI;
import net.sc.elytracomponent.api.ElytraAttachAPI;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.sc.elytracomponent.data.ElytraComponentDefinition;
import net.sc.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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

        if (!ElytraAttachAPI.canAttach(player, mainHand, offHand, chestplate)) return false;

        if (level.isClientSide) return true;

        if (ElytraAttachAPI.hasExistingComponent(chestplate)) {
            player.displayClientMessage(
                    Component.translatable("message.elytra_component.already_has_component",
                            chestplate.getHoverName()), true);
            return true;
        }

        ElytraComponentDefinition def = ElytraAttachAPI.getDefinition(mainHand);
        if (def == null) return false;

        ElytraAttachAPI.onBeforeAttach(player, chestplate, mainHand);

        // ========== 保存原始 AttributeModifiers ==========
        CompoundTag originalChestAttrs = null;
        CompoundTag chestTag = chestplate.getTag();
        if (chestTag != null && chestTag.contains("AttributeModifiers")) {
            originalChestAttrs = chestTag.getCompound("AttributeModifiers").copy();
        }

        // ========== 合并鞘翅属性到胸甲 ==========
        CompoundTag elytraTag = mainHand.getTag();
        if (elytraTag != null && elytraTag.contains("AttributeModifiers")) {
            CompoundTag merged = (originalChestAttrs != null) ? originalChestAttrs.copy() : new CompoundTag();
            mergeAttributeNBT(merged, elytraTag.getCompound("AttributeModifiers"));
            chestplate.getOrCreateTag().put("AttributeModifiers", merged);
        }

        Component chestplateName = chestplate.getHoverName();
        Component elytraName = mainHand.getHoverName();

        ElytraComponent component = ElytraComponentAPI.createComponent(mainHand, def, originalChestAttrs);
        ElytraComponentAPI.setComponent(chestplate, component);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        mainHand.shrink(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);

        if (!player.getAbilities().instabuild) {
            offHand.shrink(1);
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        }

        ElytraAttachAPI.onAfterAttach(player, chestplate, component);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);
        }

        player.displayClientMessage(
                Component.translatable("message.elytra_component.elytra_attached",
                        chestplateName, elytraName), true);

        return true;
    }

    private static void mergeAttributeNBT(CompoundTag base, CompoundTag addition) {
        ListTag baseList = base.getList("AttributeModifiers", 10);
        ListTag addList = addition.getList("AttributeModifiers", 10);
        baseList.addAll(addList);
        base.put("AttributeModifiers", baseList);
    }
}
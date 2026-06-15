package net.sc.elytracomponent.handler;

import net.minecraft.nbt.CompoundTag;
import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ElytraComponentAPI;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraDetachHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        // 检测副手是否有带组件的物品
        ItemStack offHand = player.getOffhandItem();
        if (!ModComponents.hasComponent(offHand)) return;

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        detachElytra(player, level, offHand, InteractionHand.OFF_HAND);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void detachElytra(Player player, Level level,
                                     ItemStack sourceStack, InteractionHand hand) {
        ElytraComponent component = ModComponents.getComponent(sourceStack);
        if (component == null) return;

        // ========== 恢复原始属性 ==========
        CompoundTag origAttrs = component.originalChestAttributes();
        if (origAttrs != null) {
            sourceStack.getOrCreateTag().put("AttributeModifiers", origAttrs.copy());
        } else {
            CompoundTag tag = sourceStack.getTag();
            if (tag != null) {
                tag.remove("AttributeModifiers");
                if (tag.isEmpty()) sourceStack.setTag(null);
            }
        }

        ItemStack restoredElytra = ElytraComponentAPI.restoreElytra(component);
        net.minecraft.network.chat.Component elytraName = restoredElytra.getHoverName();

        ElytraComponentAPI.removeComponent(sourceStack);

        boolean given = player.getInventory().add(restoredElytra);
        if (!given) {
            player.drop(restoredElytra, false);
        }

        player.setItemInHand(hand, sourceStack);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1
            );
        }

        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.elytra_component.elytra_detached", elytraName), true);
    }
}

package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ElytraComponentAPI;
import net.sc.elytracomponent.api.ElytraDetachAPI;
import net.sc.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.sc.elytracomponent.api.ability.IElytraAbility;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
        ItemStack offHand = player.getOffhandItem();

        if (!ElytraDetachAPI.canDetach(offHand)) return;

        ElytraComponent component = ModComponents.getComponent(offHand);
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

    private static void detachElytra(Player player, Level level, ItemStack sourceStack, InteractionHand hand) {
        ElytraComponent component = ModComponents.getComponent(sourceStack);
        if (component == null) return;

        onBeforeDetach(player, sourceStack, component);
        restoreOriginalAttributes(sourceStack, component);

        ItemStack restored = ElytraComponentAPI.restoreElytra(component);
        ElytraComponentAPI.removeComponent(sourceStack);

        ItemStack copy = restored.copy();
        boolean given = player.getInventory().add(copy);
        if (!given) player.drop(copy, false);

        player.setItemInHand(hand, sourceStack);
        onAfterDetach(player, level, restored);
    }

    private static void onBeforeDetach(Player player, ItemStack sourceStack, ElytraComponent component) {
        CompoundTag abilityConfig = component.abilityConfig();
        if (abilityConfig != null && abilityConfig.contains("type")) {
            IElytraAbility ability = ElytraAbilityRegistry.create(abilityConfig.getString("type"));
            if (ability != null) ability.onUnequip(player, sourceStack, component);
        }
        ElytraDetachAPI.onBeforeDetach(player, sourceStack, component);
    }

    private static void restoreOriginalAttributes(ItemStack chestplate, ElytraComponent component) {
        CompoundTag origAttrs = component.originalChestAttributes();
        if (origAttrs != null) {
            chestplate.getOrCreateTag().put("AttributeModifiers", origAttrs.copy());
        } else {
            CompoundTag tag = chestplate.getTag();
            if (tag != null) {
                tag.remove("AttributeModifiers");
                if (tag.isEmpty()) chestplate.setTag(null);
            }
        }
    }

    private static void onAfterDetach(Player player, Level level, ItemStack restored) {
        if (restored.isEmpty()) return;
        Component name = restored.getHoverName();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1);
        }
        player.displayClientMessage(
                Component.translatable("message.elytra_component.elytra_detached", name), true);
        ElytraDetachAPI.onAfterDetach(player, restored);
    }
}
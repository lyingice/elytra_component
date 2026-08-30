package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/**
 * 经验修补联动：胸甲带修补附魔时，经验球优先修复鞘翅组件耐久（1 XP = 2 点耐久），
 * 修复用不完的经验返还给玩家。胸甲本体的原版修补行为不受影响。
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraMendingHandler {

    /** 每 1 点经验可修复的组件耐久 */
    private static final int DURABILITY_PER_XP = 2;

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;
        var mendingHolder = player.level().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .get(Enchantments.MENDING).orElse(null);
        if (mendingHolder == null || chestplate.getEnchantmentLevel(mendingHolder) <= 0) return;

        ElytraComponent component = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;

        int max = component.maxDurability();
        if (max <= 0) return; // 无耐久限制，无需修补
        int deficit = max - component.currentDurability();
        if (deficit <= 0) return; // 组件完好，交给原版逻辑（修胸甲本体/加经验）

        int xpValue = event.getOrb().getValue();
        int repair = Math.min(deficit, xpValue * DURABILITY_PER_XP);
        if (repair <= 0) return;

        ElytraComponent repaired = component.withDurability(component.currentDurability() + repair);
        chestplate.set(ModComponents.ELYTRA_COMPONENT.get(), repaired);

        // 吃掉经验球，剩余经验返还玩家
        event.setCanceled(true);
        event.getOrb().discard();

        int consumedXp = (repair + DURABILITY_PER_XP - 1) / DURABILITY_PER_XP;
        if (consumedXp < xpValue && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperiencePoints(xpValue - consumedXp);
        }
    }
}

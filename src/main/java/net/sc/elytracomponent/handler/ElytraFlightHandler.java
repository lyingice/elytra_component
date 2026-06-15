package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 处理安装了 ElytraComponent 的胸甲的飞行耐久消耗。
 * - 每 10 tick 消耗 1 点组件耐久
 * - 创造模式/旁观者不消耗
 * - 组件耐久耗尽时停止飞行
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraFlightHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Player player = event.player;
        if (player.level().isClientSide) return;

        // 创造模式/旁观者模式不消耗耐久
        if (player.getAbilities().instabuild) return;
        if (player.isSpectator()) return;

        // 必须穿着有组件的胸甲
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ModComponents.hasComponent(chestStack)) return;

        // 必须在飞行中
        if (!player.isFallFlying()) return;

        ElytraComponent component = ModComponents.getComponent(chestStack);
        if (component == null) return;

        // 每 10 tick 消耗 1 点耐久
        if (player.tickCount % 10 == 0) {
            int newDurability = component.currentDurability() - 1;

            if (newDurability <= 0) {
                // 耐久耗尽
                ElytraComponent brokenComponent = new ElytraComponent(
                        component.sourceNamespace(),
                        component.originalElytraId(),
                        component.originalElytraTag(),
                        0,
                        component.maxDurability(),
                        component.textureOverride(),
                        component.extraData(),
                        component.originalChestAttributes(),
                        component.abilityConfig(),
                        component.particleConfig()
                );
                ModComponents.setComponent(chestStack, brokenComponent);
            } else {
                // 正常消耗
                ElytraComponent updatedComponent = new ElytraComponent(
                        component.sourceNamespace(),
                        component.originalElytraId(),
                        component.originalElytraTag(),
                        newDurability,
                        component.maxDurability(),
                        component.textureOverride(),
                        component.extraData(),
                        component.originalChestAttributes(),
                        component.abilityConfig(),
                        component.particleConfig()
                );
                ModComponents.setComponent(chestStack, updatedComponent);

                // 每 20 tick 触发滑翔游戏事件
                if (player.tickCount % 20 == 0) {
                    player.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
                }
            }
        }
    }
}

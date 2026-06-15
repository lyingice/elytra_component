package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 处理安装了 ElytraComponent 的胸甲的飞行耐久消耗。
 * - 每 10 tick 消耗 1 点组件耐久
 * - 创造模式/旁观者不消耗
 * - 组件耐久耗尽时停止飞行
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID)
public class ElytraFlightHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living.level().isClientSide) return;

        // 创造模式/旁观者模式不消耗耐久
        if (living instanceof Player player && player.getAbilities().instabuild) return;
        if (living.isSpectator()) return;

        // 必须穿着有组件的胸甲
        ItemStack chestStack = living.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestStack.has(ModComponents.ELYTRA_COMPONENT.get())) return;

        // 必须在飞行中
        if (!living.isFallFlying()) return;

        ElytraComponent component = chestStack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return;
        // 每 10 tick 消耗 1 点耐久
        if (living.tickCount % 10 == 0) {
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
                chestStack.set(ModComponents.ELYTRA_COMPONENT.get(), brokenComponent);
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
                chestStack.set(ModComponents.ELYTRA_COMPONENT.get(), updatedComponent);

                // 每 20 tick 触发滑翔游戏事件
                if (living.tickCount % 20 == 0) {
                    living.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
                }
            }
        }
    }
}
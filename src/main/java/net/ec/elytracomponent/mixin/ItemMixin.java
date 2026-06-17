package net.ec.elytracomponent.mixin;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements IItemExtension {

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component != null && component.currentDurability() > 0) {
            CompoundTag config = component.abilityConfig();
            if (config != null && config.contains("type")) {
                return false; // 有能力接管，不触发原版飞行检测
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component != null && component.currentDurability() > 0) {
            // 只维持飞行，不消耗耐久（耐久由 ElytraFlightHandler 统一处理）
            return true;
        }
        return false;
    }
}
package net.ec.elytracomponent.mixin;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements IForgeItem {

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        ElytraComponent component = ModComponents.getComponent(stack);
        if (component == null) return false;
        // maxDurability<=0 视为无耐久限制，直接可飞
        return component.maxDurability() <= 0 || component.currentDurability() > 0;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        ElytraComponent component = ModComponents.getComponent(stack);
        if (component == null) return false;
        // 只维持飞行，不消耗耐久（耐久由 ElytraFlightHandler/能力统一处理）
        return component.maxDurability() <= 0 || component.currentDurability() > 0;
    }
}


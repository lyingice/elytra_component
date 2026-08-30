package net.ec.elytracomponent.api.flight.sources;

import net.ec.elytracomponent.api.flight.ISource;
import net.ec.elytracomponent.api.flight.IJetpack.Context;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class EquipmentSource implements ISource {
    private final EquipmentSlot slot;
    private final ItemStack stack;

    public EquipmentSource(EquipmentSlot slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean isDisabled(Context context) {
        // If the item in the slot changed (e.g. stack became empty), the source is disabled
        return stack.isEmpty();
    }
}

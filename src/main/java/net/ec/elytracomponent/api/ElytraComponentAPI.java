package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class ElytraComponentAPI {

    public static void register(String componentId, ElytraComponentDefinition def) {
        ElytraComponentReloadListener.registerDirectly(componentId, def);
    }

    public static boolean isRegisteredElytra(ItemStack stack) {
        return ElytraComponentReloadListener.isRegisteredElytra(stack.getItem());
    }

    @Nullable
    public static ElytraComponent getComponent(ItemStack chestplate) {
        return chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
    }

    public static boolean hasComponent(ItemStack chestplate) {
        return chestplate.has(ModComponents.ELYTRA_COMPONENT.get());
    }

    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def,
                                                  @Nullable CompoundTag originalChestAttrs) {
        int maxDurability = def.durability().calculateDurability(elytraStack.getMaxDamage());
        int currentDurability = maxDurability - elytraStack.getDamageValue();
        if (currentDurability < 0) currentDurability = 0;

        ResourceLocation textureOverride = null;
        if (def.texture() != null && def.texture().elytraLayer() != null) {
            textureOverride = def.texture().elytraLayer();
        }

        return new ElytraComponent(
                def.getSourceNamespace(),
                def.elytraItem(),
                null,
                currentDurability,
                maxDurability,
                textureOverride,
                null,
                originalChestAttrs,
                null,
                null
        );
    }

    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def) {
        return createComponent(elytraStack, def, null);
    }

    public static ItemStack restoreElytra(ElytraComponent component) {
        var item = BuiltInRegistries.ITEM.get(component.originalElytraId());
        if (item == Items.AIR) return ItemStack.EMPTY;

        ItemStack elytra = new ItemStack(item, 1);

        if (elytra.isDamageableItem()) {
            int maxDamage = elytra.getMaxDamage();
            float ratio = (float) component.currentDurability() / component.maxDurability();
            int damage = maxDamage - Math.round(maxDamage * ratio);
            elytra.setDamageValue(Math.max(0, Math.min(damage, maxDamage)));
        }

        return elytra;
    }

    public static void setComponent(ItemStack chestplate, ElytraComponent component) {
        chestplate.set(ModComponents.ELYTRA_COMPONENT.get(), component);
        chestplate.set(ModComponents.CAN_ELYTRA_FLY.get(), true);
    }

    public static void removeComponent(ItemStack chestplate) {
        chestplate.remove(ModComponents.ELYTRA_COMPONENT.get());
        chestplate.remove(ModComponents.CAN_ELYTRA_FLY.get());
    }
}
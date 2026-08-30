package net.ec.elytracomponent.compat.curios;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class CuriosHelper {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("curios", "item_handler"),
                    IItemHandler.class);

    @javax.annotation.Nullable
    public static IItemHandler getCuriosInventory(Player player) {
        if (ModList.get().isLoaded("curios")) return player.getCapability(CURIOS_INVENTORY);
        return null;
    }

    public static boolean isCurioItem(ItemStack itemstack) {
        return BuiltInRegistries.ITEM.getTagNames()
                .filter(tagKey -> tagKey.location().getNamespace().equals("curios"))
                .anyMatch(itemstack::is);
    }
}

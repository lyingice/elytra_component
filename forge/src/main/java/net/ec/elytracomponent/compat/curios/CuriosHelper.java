package net.ec.elytracomponent.compat.curios;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CuriosHelper {
    private static final Logger LOGGER = LogManager.getLogger(CuriosHelper.class);
    private static Capability<IItemHandler> CURIOS_INVENTORY = null;

    static {
        try {
            if (ModList.get().isLoaded("curios")) {
                CURIOS_INVENTORY = CapabilityManager.get(new CapabilityToken<>(){});
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Curios capability: {}", e.getMessage());
            CURIOS_INVENTORY = null;
        }
    }

    @javax.annotation.Nullable
    public static IItemHandler getCuriosInventory(Player player) {
        if (CURIOS_INVENTORY != null && ModList.get().isLoaded("curios")) {
            return player.getCapability(CURIOS_INVENTORY).resolve().orElse(null);
        }
        return null;
    }

    public static boolean isCurioItem(ItemStack itemstack) {
        return BuiltInRegistries.ITEM.getTagNames()
                .filter(tagKey -> tagKey.location().getNamespace().equals("curios"))
                .anyMatch(itemstack::is);
    }
}

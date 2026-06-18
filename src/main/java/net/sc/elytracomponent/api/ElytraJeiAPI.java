// api/ElytraJeiAPI.java
package net.sc.elytracomponent.api;

import net.sc.elytracomponent.data.ElytraComponentDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 相关 API。
 */
public class ElytraJeiAPI {

    /** 获取有效定义 */
    public static List<ElytraComponentDefinition> getValidDefinitions(List<ElytraComponentDefinition> all) {
        List<ElytraComponentDefinition> valid = new ArrayList<>();
        for (var def : all) {
            if (!modsLoaded(def)) continue;
            if (BuiltInRegistries.ITEM.get(def.elytraItem()) == Items.AIR) continue;
            valid.add(def);
        }
        return valid;
    }

    /** 获取物品列表 */
    public static List<ItemStack> toItemStacks(List<ElytraComponentDefinition> defs) {
        return defs.stream().map(d -> new ItemStack(BuiltInRegistries.ITEM.get(d.elytraItem()))).toList();
    }

    /** 自定义描述行。附属模组覆盖此方法追加信息 */
    public static void addCustomDescription(ElytraComponentDefinition def, List<Component> lines) {}

    private static boolean modsLoaded(ElytraComponentDefinition def) {
        for (String mod : def.compatibility().requiredMods()) {
            if (!ModList.get().isLoaded(mod)) return false;
        }
        return true;
    }
}

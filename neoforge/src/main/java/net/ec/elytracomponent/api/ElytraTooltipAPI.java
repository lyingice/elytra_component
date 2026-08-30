// api/ElytraTooltipAPI.java
package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tooltip 相关 API。
 */
public class ElytraTooltipAPI {

    /** 自定义 tooltip 入口。附属模组覆盖此方法添加额外信息 */
    public static void addCustomTooltip(ItemStack stack, ElytraComponent component, List<Component> tooltip) {}
}
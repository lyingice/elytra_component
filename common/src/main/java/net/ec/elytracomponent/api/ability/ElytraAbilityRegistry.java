package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 鞘翅能力注册表。附属模组在构造阶段注册自定义能力。
 */
public class ElytraAbilityRegistry {
    private static final Map<String, IElytraAbility> ABILITIES = new HashMap<>();

    public static void register(String id, IElytraAbility ability) {
        ABILITIES.put(id, ability);
    }

    @Nullable
    public static IElytraAbility create(String id) {
        return ABILITIES.get(id);
    }
}

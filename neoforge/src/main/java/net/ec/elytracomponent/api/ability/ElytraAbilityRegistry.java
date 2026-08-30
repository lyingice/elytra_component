package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.ElytraComponentMod;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 能力注册中心
 * 附属模组调用 register() 注册能力工厂
 */
public class ElytraAbilityRegistry {

    private static final Map<String, Supplier<IElytraAbility>> ABILITIES = new HashMap<>();

    /**
     * 注册能力工厂
     * @param id      对应数据包 JSON 中 ability.type
     * @param factory 能力实例工厂（每次装备时创建新实例）
     */
    public static void register(String id, Supplier<IElytraAbility> factory) {
        if (ABILITIES.containsKey(id)) {
            ElytraComponentMod.LOGGER.warn("Duplicate ability registration: {}", id);
            return;
        }
        ABILITIES.put(id, factory);
        ElytraComponentMod.LOGGER.info("Registered elytra ability: {}", id);
    }

    /**
     * 创建能力实例
     */
    public static IElytraAbility create(String id) {
        Supplier<IElytraAbility> factory = ABILITIES.get(id);
        return factory != null ? factory.get() : null;
    }

    /**
     * 检查能力是否已注册
     */
    public static boolean hasAbility(String id) {
        return ABILITIES.containsKey(id);
    }

    /**
     * 获取所有已注册的能力 ID
     */
    public static Iterable<String> getRegisteredIds() {
        return ABILITIES.keySet();
    }
}
package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.api.ability.IElytraAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ElytraAbilityRegistry {
    private static final Map<String, Supplier<IElytraAbility>> ABILITIES = new HashMap<>();

    public static void register(String id, Supplier<IElytraAbility> factory) {
        ABILITIES.put(id, factory);
    }

    public static IElytraAbility create(String id) {
        return ABILITIES.containsKey(id) ? ABILITIES.get(id).get() : null;
    }
}
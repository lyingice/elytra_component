package net.ec.elytracomponent.api.flight;

import net.ec.elytracomponent.api.flight.IJetpack.Context;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Supplier;

public interface ISource {
    boolean isDisabled(Context context);

    interface Provider {
        List<Pair<Object, ISource>> get(LivingEntity entity);
    }

    interface Caster {
        List<Supplier<IJetpack>> get(Object value);
    }

    class ProviderEntry {
        public final ISource source;
        public final Supplier<IJetpack> provider;

        public ProviderEntry(ISource source, Supplier<IJetpack> provider) {
            this.source = source;
            this.provider = provider;
        }
    }

    class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }
    }
}

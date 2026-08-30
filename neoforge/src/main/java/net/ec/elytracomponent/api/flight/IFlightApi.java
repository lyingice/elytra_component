package net.ec.elytracomponent.api.flight;

import net.ec.elytracomponent.api.flight.IJetpack.Context;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public interface IFlightApi {
    IFlightApi INSTANCE = FlightApiHolder.INSTANCE;

    void addSourceProvider(ISource.Provider provider);

    void addSourceCaster(ISource.Caster caster);

    List<ISource.ProviderEntry> getAll(LivingEntity entity);

    IJetpack.Context findJetpack(LivingEntity entity);

    boolean isActive(ControlType type, FlightKey key, LivingEntity entity);

    IJetpack.Context findActiveJetpack(LivingEntity entity);

    boolean isPressed(FlightKey key, LivingEntity entity);

    class FlightApiHolder {
        public static final IFlightApi INSTANCE = new IFlightApi() {
            private final List<ISource.Provider> providers = new ArrayList<>();
            private final List<ISource.Caster> casters = new ArrayList<>();

            @Override
            public void addSourceProvider(ISource.Provider provider) {
                providers.add(provider);
            }

            @Override
            public void addSourceCaster(ISource.Caster caster) {
                casters.add(caster);
            }

            @Override
            public List<ISource.ProviderEntry> getAll(LivingEntity entity) {
                List<ISource.Pair<Object, ISource>> objects = new ArrayList<>();
                for (var provider : providers) {
                    objects.addAll(provider.get(entity));
                }

                List<ISource.ProviderEntry> results = new ArrayList<>();
                for (var pair : objects) {
                    Object value = pair.first;
                    ISource source = pair.second;
                    for (var caster : casters) {
                        for (var supplier : caster.get(value)) {
                            results.add(new ISource.ProviderEntry(source, supplier));
                        }
                    }
                }
                return results;
            }

            @Override
            public IJetpack.Context findJetpack(LivingEntity entity) {
                var world = entity.level();
                if (world == null) return null;
                var pose = FlyingPose.get(entity);

                return getAll(entity).stream()
                        .map(entry -> {
                            IJetpack jetpack = entry.provider.get();
                            ISource source = entry.source;
                            if (jetpack == null) return null;
                            var ctx = IJetpack.Context.builder(entity, world, pose, source).build(jetpack);
                            return ctx.jetpack.isValid(ctx) ? ctx : null;
                        })
                        .filter(ctx -> ctx != null)
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public boolean isActive(ControlType type, FlightKey key, LivingEntity entity) {
                return switch (type) {
                    case ALWAYS -> true;
                    case NEVER -> false;
                    case TOGGLE -> key.isPressed(entity);
                };
            }

            @Override
            public IJetpack.Context findActiveJetpack(LivingEntity entity) {
                if (entity instanceof net.minecraft.world.entity.player.Player player && player.getAbilities().flying) return null;
                var ctx = findJetpack(entity);
                if (ctx == null) return null;
                if (!isActive(ctx.jetpack.activeType(ctx), FlightKey.TOGGLE_ACTIVE, entity)) return null;
                if (!ctx.jetpack.isUsable(ctx)) return null;
                if (ctx.source.isDisabled(ctx)) return null;
                return ctx;
            }

            @Override
            public boolean isPressed(FlightKey key, LivingEntity entity) {
                return net.ec.elytracomponent.api.flight.logic.ControlManager.isPressed(key, entity);
            }
        };
    }

    static void register(IFlightApi api) {
        // Registration is handled via the singleton holder
    }
}

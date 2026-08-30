package net.ec.elytracomponent.api.flight.sources;

import net.ec.elytracomponent.api.flight.ISource;
import net.ec.elytracomponent.api.flight.IJetpack.Context;

public class EntitySource implements ISource {
    public static final EntitySource INSTANCE = new EntitySource();

    private EntitySource() {}

    @Override
    public boolean isDisabled(Context context) {
        return false;
    }
}

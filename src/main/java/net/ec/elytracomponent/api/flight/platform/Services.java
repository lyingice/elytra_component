package net.ec.elytracomponent.api.flight.platform;

import net.ec.elytracomponent.api.flight.platform.services.IPlatformHelper;
import net.ec.elytracomponent.api.flight.platform.services.IRegistries;

/**
 * Direct service access for flight lib.
 * Since this is a NeoForge-only mod, we instantiate implementations directly.
 */
public class Services {
    public static final IPlatformHelper PLATFORM = new ForgePlatformHelper();
    public static final IRegistries REGISTRIES = new ForgeRegistries();
}

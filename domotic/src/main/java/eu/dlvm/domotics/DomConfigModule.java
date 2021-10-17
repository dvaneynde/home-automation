package eu.dlvm.domotics;

import com.google.inject.AbstractModule;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;

public class DomConfigModule extends AbstractModule {
    private final DomConfig config;

    DomConfigModule(DomConfig config) {
        this.config = config;
    }

    @Override public void configure() {
        // Because you're binding to a single instance, you always get the
        // exact same one back from Guice. This makes it implicitly a singleton.
        bind(DomConfig.class).toInstance(config);
    }
}

package eu.dlvm.domotics;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.diamondsys.messaging.HardwareIO;

public class BasicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Domotic.class).in(Scopes.SINGLETON);//.asEagerSingleton();
        bind(IHardwareIO.class).to(HardwareIO.class).in(Scopes.SINGLETON);
    }
}

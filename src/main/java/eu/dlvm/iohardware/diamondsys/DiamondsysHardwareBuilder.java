package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.IHardwareBuilder;
import eu.dlvm.iohardware.IHardware;
import eu.dlvm.iohardware.diamondsys.factories.XmlHwConfigurator;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverChannelSimulator;
import eu.dlvm.iohardware.diamondsys.messaging.HwDriverTcpChannel;
import eu.dlvm.iohardware.diamondsys.messaging.IHwDriverChannel;

public class DiamondsysHardwareBuilder implements IHardwareBuilder {

    @Override
    public IHardware build(String cfgFile, String host, int port, int readTimeout, boolean simulated) {
        XmlHwConfigurator xhc = new XmlHwConfigurator(cfgFile);
        IHwDriverChannel hdc;
        if (simulated)
            hdc = new HwDriverChannelSimulator();
        else
            hdc = new HwDriverTcpChannel(host, port, readTimeout);
        DiamondsysHardware hw = new DiamondsysHardware(xhc, hdc);
        return hw;
    }
}

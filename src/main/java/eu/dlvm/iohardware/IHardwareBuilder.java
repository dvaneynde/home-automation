package eu.dlvm.iohardware;

/**
 * A hardware specific instance of this interface can be used to bootstrap access to a hardware specific {@link IHardware}.
 * <p>Note that this is just a start, to really be hardware independent the paraneters should be passed as a key-value list, and domotic's main should accept these as an encoded script or so. And the DiamondSys stuff should be in a jar loaded at startup.
 */
public interface IHardwareBuilder {
    /**
     * Create hardware-specific singleton {@link IHardware}.
     * @param cfgFile Path to hardware configuration file.
     * @param host Hostname where hardware driver (HwDriver) runs.
     * @param port Port where hardware driver (HwDriver) listens on.
     * @param readTimeout TODO
     * @param simulated If hardware is to be simulated.
     * @return
     */
    IHardware build(String cfgFile, String host, int port, int readTimeout, boolean simulated);
}

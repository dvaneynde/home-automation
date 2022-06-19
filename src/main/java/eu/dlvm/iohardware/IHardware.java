package eu.dlvm.iohardware;

/**
 * Abstraction of the I/O Hardware.
 * Typical sequence:
 * <ol>
 * <li>{@link #initialize()}</li>
 * <li>In a loop, use {@link IHardwareReader} and {@link IHardwareWriter} continuously, until stop is requested.
 * <li>{@link #stop()}</li>
 * </ol>
 * 
 * @author Dirk Vaneynde
 */

public interface IHardware  {

	/**
	 * Connect to hardware driver and initialize hardware.
	 */
	public void initialize() throws ChannelFault;

	/**
	 * Stop hardware and disconnect from hardware driver.
	 */
	public void stop();
	
	public IHardwareReader getReader();
	
	public IHardwareWriter getWriter();
}

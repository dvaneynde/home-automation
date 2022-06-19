package eu.dlvm.iohardware;

/**
 * Abstraction of hardware inputs, being digital (0/1) or analog (0..n).
 * <p> Each input is identified by its channel ID, a {@link String}.
 * <p>Input channels and output
 * channels are different; so channel 'ABC' can be used twice, once for input
 * and once for output.
 * <p>
 * To optimize interaction with the hardware inputs and outputs are buffered.
 * Only when calling {@link #refreshInputs()} the inputs are synchronized with the hardware.
 * 
 * @author Dirk Vaneynde
 */

public interface IHardwareReader {

	/**
	 * Reads all hardware inputs, refreshing local buffer.
	 */
	public void refreshInputs();

	/**
	 * Returns the state of a digital input.
	 * 
	 * @param channel
	 * @return On (true) or off.
	 * @throws IllegalArgumentException
	 *                                  Logical channel does not have a physical
	 *                                  one.
	 */
	public boolean readDigitalInput(String channel)
			throws IllegalArgumentException;

	/**
	 * Returns the measured value of given channel.
	 * 
	 * @param channel
	 * @return Value of input channel.
	 * @throws IllegalArgumentException
	 *                                  Logical channel does not have a physical
	 *                                  one.
	 */
	public int readAnalogInput(String channel) throws IllegalArgumentException;

}

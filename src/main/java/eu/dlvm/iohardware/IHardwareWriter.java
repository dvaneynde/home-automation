package eu.dlvm.iohardware;

/**
 * Abstraction of hardware outputs, being digital (0/1) or analog (0..n).
 * <p> Each output is identified by its channel ID, a {@link String}.
 * <p> Input channels and output
 * channels are different; so channel 'ABC' can be used twice, once for input
 * and once for output.
 * <p>
 * To optimize interaction with the hardware inputs and outputs are buffered.
 * Only when calling {@link #refreshOutputs()} the outputs are synchronized with the hardware.
 * 
 * @author Dirk Vaneynde
 */

public interface IHardwareWriter {

	/**
	 * Writes all hardware outputs, getting them in line with local buffer.
	 */
	public void refreshOutputs();

	/**
	 * Outputs given boolean value on given channel.
	 * 
	 * @param channel
	 *            Logical channel, will be properly converted by hardware
	 *            implementation.
	 * @param value
	 *            Well, on or off.
	 * @throws IllegalArgumentException
	 *             Logical channel does not have a physical one.
	 */
	public void writeDigitalOutput(String channel, boolean value)
			throws IllegalArgumentException;

	/**
	 * Outputs given analog value on given channel.
	 * 
	 * @param channel
	 *            Logical channel, will be properly converted by hardware
	 *            implementation.
	 * @param value
	 *            Any integer, should be in limits of specific hardware - see
	 *            exception.
	 * @throws IllegalArgumentException
	 *             Value not within range, or logical channel does not have a
	 *             physical one.
	 */
	public void writeAnalogOutput(String channel, int value)
			throws IllegalArgumentException;

}

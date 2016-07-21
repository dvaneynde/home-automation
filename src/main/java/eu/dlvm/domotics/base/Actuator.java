package eu.dlvm.domotics.base;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

/**
 * Actuators actuate output.
 * <p>
 * They have at least one output channel. They control their own state, control
 * the hardware, have a fairly high-level input to change their state.
 * <p>
 * Rules:
 * <ol>
 * <li>Only Actuators change outputs.</li>
 * <li>Only Actuators should use the Hardware layers output operations. These
 * must only be called within an {@link #loop(long, long)} call.</li>
 * <li></li>
 * </ol>
 * 
 * @author Dirk Vaneynde
 */
public abstract class Actuator extends Block implements IDomoticLoop, IUserInterfaceAPI {

	private static Logger log = LoggerFactory.getLogger(Actuator.class);

	private LogCh channel;
	private IDomoticContext ctx;

	/**
	 * Create Actuator Block, i.e. a Block that abstracts output devices like
	 * Lamps (1 relay), Vents (1 relay with timers), Screens (2 relays) etc.
	 * 
	 * @param channel
	 *            Output channel in Hardware that corresponds to this Actuator.
	 */
	public Actuator(String name, String description, String ui, LogCh channel, IDomoticContext ctx) {
		super(name, description, ui);
		this.ctx = ctx;
		this.channel = channel;
		ctx.addActuator(this);
	}

	/**
	 * @return Logical channel that this Sensor is connected on.
	 */
	public LogCh getChannel() {
		return channel;
	}

	/**
	 * @return Underlying hardware.
	 */
	public IHardwareIO getHw() {
		return ctx.getHw();
	}

	/**
	 * After hardware is initialized, this function may set default output
	 * values, to be picked up in the next {@link #loop(long, long)}.
	 * 
	 * @param last
	 *            known state, or <code>null</code> if first time or unknown
	 */
	public abstract void initializeOutput(RememberedOutput ro);

	/**
	 * Optional. For safeguarding, so output state <i>may</i> be used at
	 * initialization time in {@link #initializeOutput()}.
	 * 
	 * @return current actuator output
	 */
	public RememberedOutput dumpOutput() {
		return null;
	}

	@Override
	public String toString() {
		return "Actuator name='" + name + "', ch=" + getChannel() + ", description='" + description + "'";
	}

}

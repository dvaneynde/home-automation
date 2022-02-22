package eu.dlvm.domotics.actuators;

import eu.dlvm.iohardware.IHardwareWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticBuilder;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoOnOff;

/**
 * A Fan that runs for a {@link #getOnDurationSec()} seconds when toggled on.
 * <P>
 * The fan also supports delayed on and off, useful when connected to a Lamp (or
 * any other event source): if the lamp goes on then after
 * {@link #getDelayToOnSec()} the fan will start running and goes off after
 * the lamp was out for {@link #getDelayToOffSec()} seconds.
 * <p>
 * The state diagram below shows all possibilities.
 * <p>
 * <img src="./doc-files/FanStateChart.jpg">
 * <p>
 * <ul>
 * <li>TODO</li>
 * <li></li>
 * </ul>
 * 
 * @author Dirk Vaneynde
 */

public class Fan extends Actuator implements IEventListener, IUiCapableBlock {

	static Logger logger = LoggerFactory.getLogger(Fan.class);

	/**
	 * Default time fan will be held off to run when delay-on event (typically
	 * connected lamp going on) has been received.
	 */
	public static final int DEFAULT_DELAY_TO_ON_SEC = 2 * 60;
	/**
	 * Default time fan keeps running when delay-off event (typically connected
	 * lamp going off) has been received.
	 */
	public static final int DEFAULT_DELAY_TO_OFF_SEC = 3 * 60;
	/**
	 * Default running period for a Fan.
	 */
	public static final int DEFAULT_ON_DURATION_SEC = 7 * 60;

	
	private FanStatemachine statemachine;
	private long onDurationMs = DEFAULT_ON_DURATION_SEC * 1000L;
	private long delayToOnMs = DEFAULT_DELAY_TO_ON_SEC * 1000L;
	private long delayToOffMs = DEFAULT_DELAY_TO_OFF_SEC * 1000L;

	/**
	 * Constructor.
	 */
	public Fan(String name, String description, String channel, IHardwareWriter writer, IDomoticBuilder builder) {
		super(name, description, null, channel, writer, builder);
		statemachine = new FanStatemachine(this);
	}

	// ========== Configuration

	public long getOnDurationMs() {
		return onDurationMs;
	}

	public long getOnDurationSec() {
		return onDurationMs / 1000L;
	}

	public void setOnDurationSec(long sec) {
		this.onDurationMs = sec * 1000L;
	}

	public Fan overrideOnDurationSec(long sec) {
		setOnDurationSec(sec);
		return this;
	}


	public long getDelayToOnMs() {
		return delayToOnMs;
	}

	public long getDelayToOnSec() {
		return delayToOnMs / 1000L;
	}

	public void setDelayToOnSec(long sec) {
		this.delayToOnMs = sec * 1000L;
	}

	public Fan overrideDelayToOnSec(long sec) {
		setDelayToOnSec(sec);
		return this;
	}


	public long getDelayToOffMs() {
		return delayToOffMs;
	}

	public long getDelayToOffSec() {
		return delayToOffMs / 1000L;
	}

	public void setDelayToOffSec(long sec) {
		this.delayToOffMs = sec * 1000L;
	}

	public Fan overrideDelayToOffSec(long sec) {
		setDelayToOffSec(sec);
		return this;
	}


	@Override
	public void initializeOutput(RememberedOutput ro) {
		writeOutput(false);
	}


	// ========== Queries
	/**
	 * @return State of fan.
	 */
	public FanStatemachine.States getState() {
		return statemachine.getState();
	}

	public boolean isOn() {
		return statemachine.isFanning();
	}

	// ========== Events

	public boolean toggle() {
		return statemachine.toggle();
	}

	public void reallyOff() {
		statemachine.reallyOff();
	}

	public void delayOn() {
		statemachine.delayOn();
	}

	public void delayOff() {
		statemachine.delayOff();
	}

	/**
	 * Reacts on {@see IOnOffToggle.ActionType} events, with some pecularities:
	 * <ul>
	 * <li>ON: is not supported</li>
	 * <li>TOGGLE: see state chart</li>
	 * <li>OFF: calls {@link #reallyOff()}</li>
	 * </ul>
	 */
	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
			case OFF:
				reallyOff();
				break;
			case TOGGLE:
				toggle();
				break;
			case DELAY_ON:
				delayOn();
				break;
			case DELAY_OFF:
				delayOff();
				break;
			default:
				logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	// ===== UI =====

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOff uiInfo = new UiInfoOnOff(this, getState().toString(), statemachine.isFanning());
		// TODO time still running, if running
		return uiInfo;
	}

	@Override
	// FIXME must only be toggle, not on or off... or reallyOff could be added
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			statemachine.toggle();
		else if (action.equalsIgnoreCase("off"))
			statemachine.toggle();
		else
			logger.warn("update on '" + getName() + "' got unsupported action '" + action + ".");
	}

	// ===== Internal =====

	@Override
	public void loop(long current) {
		statemachine.loop(current);
	}

	void writeOutput(boolean val) {
		getHwWriter().writeDigitalOutput(getChannel(), val);
	}

	@Override
	public String toString() {
		return "Fan [name=" + name + ", description=" + description + ", uiGroup=" + uiGroup + ", onDurationMs="
				+ onDurationMs + ", delayToOnDurationMs="
				+ delayToOnMs + ", delayToOffDurationMs=" + delayToOffMs + ", statemachine="
				+ statemachine + "]";
	}
}

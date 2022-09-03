package eu.dlvm.domotics.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.IBlockRegistrar;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.controllers.gadgets.GadgetSet;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;
import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Runs a set of Gadgets sequentially, once or repeatedly.
 * <p>
 * Gadgets run in an 'enabled' period [start..start+duration] where start is a
 * specific date and time, or everyday between [onTime .. offTime] where both
 * times are ms since midnight.
 * 
 * <p>
 * Actual start of a gadget sequence is dependent of configuration and can be
 * any of:
 * <ol>
 * <li>via TRIGGER event, when it occurs in the enabled period</li>
 * <li>when start time has arrived of enabled period</li>
 * <li>via ON or TOGGLE event, manual on/off; <b>Note:</b>this does not work outside enabled
 * period</li>
 * </ol>
 * <p>
 * Whenever end time has arrived the gadgets stop. It can also be that gadgets stop because they are done.
 * <p>
 * This object holds a number of {@link GadgetSet} objects that will 'run' sequentially. A {@link GadgetSet} has itself a duration (relative to start in this Controller), when passed that duration the next {@link GadgetSet} if any is processed.
 * A GadgetS has multiple {@link eu.dlvm.domotics.controllers.gadgets.IGadget} implementations that run in parallel. See also {@see Gadgets.puml} for state transitions.
 * 
 * @author dirk
 *
 */
public class GadgetController extends Controller implements IEventListener, IUiCapableBlock {

	public static final int MS_IN_DAY = 24 * 60 * 60 * 1000;

	private static Logger logger = LoggerFactory.getLogger(GadgetController.class);
	private long startTimeMs, durationMs;
	private int onTime, offTime;
	private boolean activateOnStartTime, repeat, daily;
	private boolean manualStartRequested, manualStopRequested, triggerRecorded = false;
	private long startOfSequenceMs = -1;
	private int idxInSequence = 0;
	private long runtimePastGadgets = 0L;
	private List<GadgetSet> gadgetSets = new ArrayList<>();
	private States state = States.INACTIF;

	/**
	 * 
	 * TODO alternatief, extra MANUAL_ACTIF, die los staat van tijden e.d.
	 * 
	 * @author dirk
	 */
	public enum States {
		INACTIF, WAITING_TRIGGER, ACTIF, WAITING_END
	}

	private GadgetController(String name, boolean activateOnStartTme, boolean repeat, IBlockRegistrar ctx) {
		super(name, name, null, ctx);
		this.activateOnStartTime = activateOnStartTme;
		this.repeat = repeat;
	}

	/**
	 * One-shot version, so not daily.
	 * 
	 * @param name
	 * @param startTimeMs
	 *            absolute start time since epoch  
	 * @param durationMs
	 *            duration, [startTimeMs ..(startTimeMs+durationMs)]
	 * @param activateOnStartTme
	 *            if true becomes ACTIVE right at start time, otherwise only
	 *            after TRIGGERED event
	 * @param repeat
	 *            whether to repeat changesets indefinitely (until end time that
	 *            is)
	 * @param blockRegistrar
	 */
	public GadgetController(String name, long startTimeMs, long durationMs, boolean activateOnStartTme, boolean repeat, IBlockRegistrar blockRegistrar) {
		this(name, activateOnStartTme, repeat, blockRegistrar);
		this.startTimeMs = startTimeMs;
		this.durationMs = durationMs;
		this.daily = false;
		logger.info("Registered non-daily Gadget Controller '"+name+"'");
	}

	/**
	 * Every-day-once version.
	 * 
	 * @param name
	 * @param activateOnStartTme
	 *            if true becomes ACTIVE right at start time, otherwise only
	 *            after TRIGGERED event
	 * @param repeat
	 *            whether to repeat changesets indefinitely (until end time that
	 *            is)
	 * @param onTime ms since midnight to start
	 * @param offTime ms since midnight to end
	 * @param blockRegistrar
	 */
	public GadgetController(String name, boolean activateOnStartTme, boolean repeat, int onTime, int offTime, IBlockRegistrar blockRegistrar) {
		this(name, activateOnStartTme, repeat, blockRegistrar);
		this.onTime = onTime;
		this.offTime = offTime;
		this.daily = true;
		logger.info("Registered daily Gadget Controller '"+name+"'");
	}

	public void addGadgetSet(GadgetSet e) {
		gadgetSets.add(e);
	}

	public synchronized void requestManualStart() {
		if (state == States.INACTIF || state == States.WAITING_END) {
			logger.info(getName() + " manual start requested.");
			manualStartRequested = true;
		}
	}

	public synchronized void requestManualStop() {
		if (state == States.ACTIF || state == States.WAITING_TRIGGER) {
			manualStopRequested = true;
			logger.info(getName() + " manual stop requested.");
		}
	}

	private boolean withinTimePeriod(long currentTime) {
		if (daily) {
			boolean result;
			long midnight = Timer.getTimeMsSameDayAtHourMinute(currentTime, 0, 0);
			long currentTimeInDay = currentTime - midnight;
			if (onTime <= offTime) {
				result = (currentTimeInDay >= onTime && currentTimeInDay <= offTime);
			} else {
				result = (currentTimeInDay <= offTime || currentTimeInDay >= onTime);
			}
			return result;
		} else
			return (currentTime >= startTimeMs && currentTime <= (startTimeMs + durationMs));
	}

	public void on() {
		onEvent(this, EventType.ON);
	}

	public void off() {
		onEvent(this, EventType.OFF);
	}

	public boolean toggle() {
		onEvent(this, EventType.TOGGLE);
		return (manualStartRequested == true); // isRunning() cannot work, is async
	}

	public States getState() {
		return state;
	}

	public boolean isRunning() {
		return (state == States.ACTIF);
	}

	@Override
	public void onEvent(Block source, EventType event) {
		switch (event) {
		case ON:
			requestManualStart();
			break;
		case OFF:
			requestManualStop();
			break;
		case TOGGLE:
			if (isRunning())
				requestManualStop();
			else
				requestManualStart();
			break;
		case LIGHT_LOW: // TODO via connector naar TRIGGERED
		case TRIGGERED:
			logger.info("Got " + event + " from " + source.getName() + '.');
			triggerRecorded = true;
			break;
		default:
			logger.warn("Ignored event " + event + " from " + source.getName());
		}
	}

	@Override
	public void loop(long currentTime) {
		switch (state) {
		case INACTIF:
			if (withinTimePeriod(currentTime)) {
				if (activateOnStartTime) {
					state = States.ACTIF;
					startOfSequenceMs = -1;
				} else {
					state = States.WAITING_TRIGGER;
				}
				logger.info(getName() + " - go from INACTIF to " + state + " because start time reached.");
			}
			break;
		case WAITING_TRIGGER:
			if (!withinTimePeriod(currentTime)) {
				state = States.INACTIF;
				logger.info(getName() + " - go from ACTIF to " + state + " because of end time reached.");
			} else if (manualStopRequested) {
				state = States.WAITING_END;
				logger.info(getName() + " - go from ACTIF to " + state + " because of manual stop request.");
			} else if (triggerRecorded || manualStartRequested) {
				state = States.ACTIF;
				startOfSequenceMs = -1;
				logger.info(getName() + " - from WAITING_TRIGGER to " + state + " because of " + (triggerRecorded ? "trigger" : "manual start")
						+ ", start running for max. " + durationMs / 1000 + " sec.");
			}
			break;
		case ACTIF:
			if (!withinTimePeriod(currentTime)) {
				gadgetSets.get(idxInSequence).onDone();
				state = States.INACTIF;
				logger.info(getName() + " - go from ACTIF to " + state + " because of end time reached.");
			} else if (manualStopRequested) {
				gadgetSets.get(idxInSequence).onDone();
				state = States.WAITING_END;
				logger.info(getName() + " - go from ACTIF to " + state + " because of manual stop request.");
			}
			break;
		case WAITING_END:
			if (!withinTimePeriod(currentTime)) {
				state = States.INACTIF;
				logger.info(getName() + " - go from WAITING_END to " + state + " because of end time reached.");
			} else if (manualStartRequested) {
				if (activateOnStartTime) {
					state = States.ACTIF;
					startOfSequenceMs = -1;
				} else {
					state = States.WAITING_TRIGGER;
				}
				logger.info(getName() + " - go from WAITING_END to" + state + ", due to manual start request, for max. " + durationMs / 1000 + " sec.");
			}
			break;
		default:
			break;
		}
		manualStopRequested = manualStartRequested = triggerRecorded = false;

		if (state == States.ACTIF) {
			if (startOfSequenceMs == -1) {
				// Initialize
				startOfSequenceMs = currentTime;
				idxInSequence = 0;
				runtimePastGadgets = 0;
				logger.info(getName() + " starting set " + idxInSequence + " / " + (gadgetSets.size() - 1) + " at time 0ms.");
				gadgetSets.get(0).onBefore();
			}
			long relativeTimeWithinSequence = currentTime - startOfSequenceMs;
			GadgetSet gadgetSet = gadgetSets.get(idxInSequence);

			if (relativeTimeWithinSequence >= runtimePastGadgets + gadgetSet.getDurationMs()) {
				// Current gadgetSet end time reached, go for next one if any.
				gadgetSet.onDone();
				idxInSequence++;
				if (idxInSequence >= gadgetSets.size()) {
					// All gadgetSets done
					if (repeat) {
						// Re-initialize
						startOfSequenceMs = currentTime;
						relativeTimeWithinSequence = currentTime - startOfSequenceMs;
						idxInSequence = 0;
						runtimePastGadgets = 0;
						logger.info(getName() + " repeat set " + idxInSequence + " / " + (gadgetSets.size() - 1) + " at time 0ms.");
						gadgetSet = gadgetSets.get(idxInSequence);
						gadgetSet.onBefore();
					} else {
						// Done. As long we are still within time bounds we do not go INACTIF (don't remember why)
						state = States.WAITING_END;
						logger.info(getName() + " all gadget sets done, go to " + state + " at time " + relativeTimeWithinSequence + "ms.");
					}
				} else {
					// Next gadgetSet
					runtimePastGadgets += gadgetSet.getDurationMs();
					gadgetSet = gadgetSets.get(idxInSequence);
					gadgetSet.onBefore();
					logger.info(getName() + " previous gadget set done, now starting set " + idxInSequence + " / " + (gadgetSets.size() - 1) + " at time "
							+ relativeTimeWithinSequence + "ms.");
				}
			}

			if (state == States.ACTIF) {
				gadgetSet.onBusy(relativeTimeWithinSequence - runtimePastGadgets);
			}
		}
	}

	@Override
	public UiInfo getUiInfo() {
		return null;
	}

	@Override
	public void update(String action) {
		if (action.equalsIgnoreCase("on"))
			on();
		else if (action.equalsIgnoreCase("off"))
			off();
		else
			logger.warn("update on GadgetController '" + getName() + "' got unsupported action '" + action + ".");
	}
}

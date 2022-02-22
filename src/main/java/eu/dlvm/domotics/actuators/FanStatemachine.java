package eu.dlvm.domotics.actuators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FanStatemachine {

	static Logger logger = LoggerFactory.getLogger(FanStatemachine.class);

	/**
	 * See <img src="doc-files/fan-state.png"/>
	 */
	public enum States {
		OFF,
		ON,
		OFF_DELAY2ON,
		ON_DELAY,
		ON_DELAY2OFF
	};

	private Fan fan;
	private States state;
	private long timeStateEntered = -1L;

	public FanStatemachine(Fan fan) {
		this.fan = fan;
		state = States.OFF;
	}

	// ===== Queries =====

	public States getState() {
		return state;
	}

	public boolean isFanning() {
		return (state == States.ON || state == States.ON_DELAY || state == States.ON_DELAY2OFF);
	}

	// ===== Updates =====

	private void logEvent(States oldState, String event, long timeInNewState) {
		logger.info("Fan {} - event {} [ {} -> {} ] - will take {} seconds.", fan.getName(), event, oldState.name(),
				getState().name(), timeInNewState);
	}

	private void logEvent(States oldState, String event) {
		logger.info("Fan {} - event {} [ {} -> {} ].", fan.getName(), event, oldState.name(), getState().name());
	}

	private void logEventIgnored(String event) {
		logger.info("Fan {} - event {} is ignored in current state {}.", fan.getName(), event, getState().name());
	}

	/**
	 * See state chart.
	 */
	public boolean toggle() {
		States oldState = state;
		boolean fanOn;
		switch (oldState) {
			case OFF:
				fanOn = changeState(States.ON);
				logEvent(oldState, "toggle", fan.getOnDurationSec());
				break;
			case OFF_DELAY2ON:
				fanOn = changeState(States.ON_DELAY);
				logEvent(oldState, "toggle");
				break;
			case ON_DELAY:
				fanOn = changeState(States.OFF_DELAY2ON);
				logEvent(oldState, "toggle", fan.getDelayToOffSec());
				break;
			case ON_DELAY2OFF:
				fanOn = isFanning();
				logEventIgnored("toggle");
				break;
			case ON:
				fanOn = changeState(States.OFF);
				logEvent(oldState, "toggle");
				break;
			default:
				fanOn = isFanning();
				logger.warn("Fan {} - toggle(), unexpected state {}.", fan.getName(), getState().name());
		}
		return fanOn;
	}

	/**
	 * Turns off fan, and fan will remain off until lamp is off.
	 * <p>
	 * Note that this functionality should not be known to Ria Reul or Koen
	 * Vaneynde.
	 */
	public void reallyOff() {
		switch (state) {
			case ON_DELAY:
			case ON_DELAY2OFF:
				States oldState = state;
				changeState(States.OFF);
				logEvent(oldState, "reallyOff");
				break;
			default:
				logEventIgnored("reallyOff");
				break;
		}
	}

	public void delayOn() {
		States oldState = state;
		switch (oldState) {
			case OFF:
				changeState(States.OFF_DELAY2ON);
				logEvent(oldState, "delayOn", fan.getDelayToOnSec());
				break;
			case ON:
				changeState(States.ON_DELAY);
				logEvent(oldState, "delayOn");
				break;
			default:
				logEventIgnored("delayOn");
		}
	}

	public void delayOff() {
		States oldState = state;
		switch (oldState) {
			case OFF_DELAY2ON:
				changeState(States.OFF);
				logEvent(oldState, "delayOff");
				break;
			case ON_DELAY:
				changeState(States.ON_DELAY2OFF);
				logEvent(oldState, "delayOff", fan.getDelayToOffSec());
				break;
			default:
				logEventIgnored("delayOff");
		}
	}

	private void logLoop(States oldState, long time) {
		logger.info("Fan {} - time of {} sec. passed [ {} -> {} ].", fan.getName(), time, oldState.name(),
				getState().name());
	}

	public void loop(long current) {
		States oldState = state;
		if (timeStateEntered == -1L)
			timeStateEntered = current;
		switch (state) {
			case OFF:
				break;
			case ON:
				if ((current - timeStateEntered) > fan.getOnDurationMs()) {
					changeState(States.OFF, current);
					logLoop(oldState, fan.getOnDurationSec());
				}
				break;
			case OFF_DELAY2ON:
				if ((current - timeStateEntered) > fan.getDelayToOnMs()) {
					changeState(States.ON_DELAY, current);
					logLoop(oldState, fan.getDelayToOnMs() / 1000L);
				}
				break;
			case ON_DELAY:
				// if running too long, without delay-off, then stop too
				long timeOut = fan.getOnDurationMs() + fan.getDelayToOffMs();
				if ((current - timeStateEntered) > timeOut) {
					changeState(States.OFF, current);
					logLoop(oldState, timeOut);
				}
				break;
			case ON_DELAY2OFF:
				if ((current - timeStateEntered) > fan.getDelayToOffMs()) {
					changeState(States.OFF, current);
					logLoop(oldState, fan.getDelayToOffSec());
				}
				break;
			default:
				throw new RuntimeException();
		}
	}

	// ===== Internal =====

	private boolean changeState(States target) {
		return changeState(target, -1L);
	}

	private boolean changeState(States target, long currentTime) {
		state = target;
		timeStateEntered = currentTime;
		boolean fanning = isFanning();
		fan.writeOutput(fanning);
		return fanning;
	}

	@Override
	public String toString() {
		return "FanStatemachine [state=" + state + ", timeStateEntered=" + timeStateEntered + "]";
	}

}

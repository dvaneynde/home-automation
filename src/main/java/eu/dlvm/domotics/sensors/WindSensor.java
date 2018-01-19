package eu.dlvm.domotics.sensors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.base.Sensor;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.service.uidata.UiInfo;
import eu.dlvm.domotics.service.uidata.UiInfoOnOffLevel;

/**
 * As soon as wind speed (actually rotations per second of a gauge) is above
 * {@link #getHighFreqThreshold()} the state becomes {@link States#ALARM}. If
 * then it stays for at least {@link #getLowTimeToResetAlertSec()} seconds below
 * {@link #getLowFreqThreshold()} - during which the state is
 * {@link States#ALARM_BUT_LOW} - state goes back to {@link States#NORMAL}.
 * <p>
 * TODO misschien als N keer boven threshold gedurende M seconden, dan pas
 * alarm?
 * 
 * @author Dirk Vaneynde
 */
public class WindSensor extends Sensor implements IUiCapableBlock {

	private static final int DEFAULT_REPEAT_EVENT_MS = 1000;
	private static final Logger log = LoggerFactory.getLogger(WindSensor.class);
	private static final Logger logwind = LoggerFactory.getLogger("WIND");
	private int highFreqThreshold, lowFreqThreshold;
	private long lowTimeToResetAlertMs;
	private States state;
	private double freq;
	private long timeCurrentStateStarted, timeSinceLastEventSent;

	FrequencyGauge gauge; // package scope for unit tests

	// ===================
	// PUBLIC API

	public static enum States {
		NORMAL, ALARM, ALARM_BUT_LOW,
	};

	/**
	 * @param name
	 * @param description
	 * @param channel
	 * @param ctx
	 * @param highFreqThreshold
	 * @param lowFreqThreshold
	 * @param highTimeBeforeAlert
	 *            Unit is seconds.
	 * @param lowTimeToResetAlert
	 *            Unit is seconds.
	 */
	public WindSensor(String name, String description, String channel, IDomoticContext ctx, int highFreqThreshold, int lowFreqThreshold,
			int lowTimeToResetAlert) {
		this(name, description, null, channel, ctx, highFreqThreshold, lowFreqThreshold, lowTimeToResetAlert);
	}

	public WindSensor(String name, String description, String ui, String channel, IDomoticContext ctx, int highFreqThreshold, int lowFreqThreshold,
			int lowTimeToResetAlert) {
		super(name, description, ui, channel, ctx);
		if (highFreqThreshold < lowFreqThreshold) // TODO higfreq must be 'far' below 1/(2 xlooptime)
			throw new RuntimeException("Configuration error: highSpeedThreshold must be lower than lowSpeedThreshold.");
		this.highFreqThreshold = highFreqThreshold;
		this.lowFreqThreshold = lowFreqThreshold;
		this.lowTimeToResetAlertMs = lowTimeToResetAlert * 1000L;

		// TODO 25 below as parameter? or calculated from sample time and lowfreq/highfreq?
		this.gauge = new FrequencyGauge(25);

		timeCurrentStateStarted = timeSinceLastEventSent = 0L;
		this.state = States.NORMAL;
		// this.lastFrequency = 0L;
		log.info("Windsensor '" + getName() + "' configured: highFreq=" + getHighFreqThreshold() + ", lowFreq=" + getLowFreqThreshold()
				+ ", lowTimeToResetAlert=" + getLowTimeToResetAlertSec() / 1000.0 + "(s).");
	}

	/**
	 * @return high frequency limit
	 */
	public int getHighFreqThreshold() {
		return highFreqThreshold;
	}

	/**
	 * @return low frequency limit
	 */
	public int getLowFreqThreshold() {
		return lowFreqThreshold;
	}

	/**
	 * @return the low time to reset alert, in milliseconds
	 */
	public long getLowTimeToResetAlertSec() {
		return lowTimeToResetAlertMs;
	}

	/**
	 * @return the state
	 */
	public States getState() {
		return state;
	}

	public int getFreqTimesHundred() {
		return (int) (freq * 100);
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfoOnOffLevel uiInfo = new UiInfoOnOffLevel(this, getState().toString(), true, getFreqTimesHundred());
		return uiInfo;
	}

	@Override
	public void update(String action) {
	}

	@Override
	public String toString() {
		return "WindSensor [highFreqThreshold=" + highFreqThreshold + ", lowFreqThreshold=" + lowFreqThreshold + ", lowTimeToResetAlertMs="
				+ lowTimeToResetAlertMs + ", state=" + state + ", freq=" + freq + "]";
	}

	// ===================
	// INTERNAL API

	// for logging only, 4 times per second
	private int infoCounter = 0;

	@Override
	public void loop(long currentTime, long sequence) {
		boolean newInput = getHw().readDigitalInput(getChannel());
		gauge.sample(currentTime, newInput);
		freq = gauge.getMeasurement();

		if (log.isDebugEnabled())
			logwind.debug(state + "\ttime=\t" + (currentTime / 1000) % 1000 + "s.\t" + currentTime % 1000 + "ms.\tfreq=" + freq);
		else if ((currentTime % 1000) >= (infoCounter * 250)) {
			if (freq > 0.0)
				logwind.info(state + "\ttime=\t" + (currentTime / 1000) % 1000 + "s.\t" + currentTime % 1000 + "ms.\tfreq=" + freq);
			infoCounter = (infoCounter + 1) % 4;
		}

		switch (state) {
		case NORMAL:
			if (freq >= getHighFreqThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName() + "' notifies HIGH event because in ALARM state: freq=" + freq + " > thresholdHigh="
						+ getHighFreqThreshold() + " for just one measurement.");
				notifyListeners(EventType.ALARM);
			}
			break;
		case ALARM:
			if (freq < getLowFreqThreshold()) {
				state = States.ALARM_BUT_LOW;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM to ALARM_BUT_LOW: freq=" + freq + " <= thresholdLow=" + getLowFreqThreshold());
			}
			break;
		case ALARM_BUT_LOW:
			if (freq >= getHighFreqThreshold()) {
				state = States.ALARM;
				timeCurrentStateStarted = currentTime;
				log.debug("WindSensor '" + getName() + "': ALARM_BUT_LOW to ALARM: freq=" + freq + " > thresholdHigh=" + getHighFreqThreshold());
			} else if ((currentTime - timeCurrentStateStarted) > getLowTimeToResetAlertSec()) {
				state = States.NORMAL;
				timeCurrentStateStarted = timeSinceLastEventSent = currentTime;
				log.info("WindSensor '" + getName() + "' notifies LOW event because wind has been low long enough: freq=" + freq + " < thresholdLow="
						+ getLowFreqThreshold());
				notifyListeners(EventType.SAFE);
			}
			break;
		}
		if (currentTime - timeSinceLastEventSent >= DEFAULT_REPEAT_EVENT_MS) {
			notifyListeners((state == States.ALARM || state == States.ALARM_BUT_LOW) ? EventType.ALARM : EventType.SAFE);
			timeSinceLastEventSent = currentTime;

		}
	}
}

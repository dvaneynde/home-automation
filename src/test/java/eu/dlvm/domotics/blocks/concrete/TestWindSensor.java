package eu.dlvm.domotics.blocks.concrete;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoContextMock;
import eu.dlvm.domotics.sensors.IAlarmListener;
import eu.dlvm.domotics.sensors.WindSensor;
import eu.dlvm.domotics.sensors.WindSensor.States;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public class TestWindSensor implements IAlarmListener {

	public class Hardware extends BaseHardwareMock implements IHardwareIO {
		public boolean inval;

		@Override
		public void writeDigitalOutput(LogCh channel, boolean value) throws IllegalArgumentException {
			inval = value;
		}

		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inval;
		}
	};

	static final long SAMPLE_TIME = 20;
	static final LogCh WINDSENSOR_CH = new LogCh(10);

	private static final Logger log = LoggerFactory.getLogger(TestWindSensor.class);
	private Hardware hw;
	private IDomoticContext dom;
	private WindSensor ws;
	private long time;
	private IAlarmListener.EventType lastEvent;
	private int nrEvents;

	// private long seq, cur;

	@Override
	public void onEvent(Block source, EventType event) {
		lastEvent = event;
		nrEvents++;
	}

	/**
	 * @param freq
	 *            frequency to simulate
	 * @param durationMs
	 *            duration of this frequency
	 * @param currentTime
	 *            start time
	 * @return end time
	 */
	void simulateWind(double freq, double durationMs) {
		double transitionPeriodMs = 1000 / (2.0 * freq);
		int nrTransitions = 1;
		long beginTime = time;
		double nextTransitionTime = beginTime + nrTransitions * transitionPeriodMs;

		boolean val = false;
		for (time = beginTime; time <= beginTime + durationMs; time += SAMPLE_TIME) {
			if (time >= nextTransitionTime) {
				val = !val;
				nrTransitions++;
				nextTransitionTime = beginTime + nrTransitions * transitionPeriodMs;
			}
			ws.loop(time, 0);
			hw.writeDigitalOutput(WINDSENSOR_CH, val);
		}
	}

	@Before
	public void init() {
		hw = new Hardware();
		dom = new DomoContextMock(hw);
	}

	// ===============
	// TESTS

	@Ignore
	@Test
	public final void simpleTest() {
		int HIGH_FREQ_THRESHOLD = 5;
		int LOW_FREQ_THRESHOLD = 2;
		int HIGH_TIME_BEFORE_ALERT = 1;
		int LOW_TIME_TO_RESET_ALERT = 2;

		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, dom, HIGH_FREQ_THRESHOLD,
				LOW_FREQ_THRESHOLD, HIGH_TIME_BEFORE_ALERT, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug("\n=============\nSTART LOW FREQ " + LOW_FREQ_THRESHOLD + " FOR 5 SEC\n=============");
		// frequency gauge op snelheid brengen
		simulateWind(LOW_FREQ_THRESHOLD, 5000);
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug("\n=============\nSTART HIGH FREQ " + HIGH_FREQ_THRESHOLD
				+ "+2 but just not long enough for alarm \n=============");
		simulateWind(HIGH_FREQ_THRESHOLD + 2, HIGH_TIME_BEFORE_ALERT * 1000 - 100);
		Assert.assertEquals(WindSensor.States.HIGH, ws.getState());

		log.debug("\n=============\n LOW FREQ " + LOW_FREQ_THRESHOLD + " FOR 5 SEC\n=============");
		// frequency gauge op snelheid brengen
		simulateWind(LOW_FREQ_THRESHOLD, 5000);
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());

		log.debug(
				"\n=============\nMUST GO TO ALARM HIGH FREQ " + HIGH_FREQ_THRESHOLD + "+2 long enough\n=============");
		simulateWind(HIGH_FREQ_THRESHOLD + 2, HIGH_TIME_BEFORE_ALERT * 1000 + 1000);
		Assert.assertEquals(WindSensor.States.ALARM, ws.getState());

		log.debug("\n=============\n LOW FREQ " + LOW_FREQ_THRESHOLD + " FOR 5 SEC\n=============");
		simulateWind(LOW_FREQ_THRESHOLD, LOW_TIME_TO_RESET_ALERT * 1000 - 100);
		Assert.assertEquals(WindSensor.States.ALARM_BUT_LOW, ws.getState());
		simulateWind(LOW_FREQ_THRESHOLD, 1000);
		Assert.assertEquals(WindSensor.States.NORMAL, ws.getState());
	}

	@Test()
	public final void testSafeAlarmSafe() {
		int HIGH_FREQ_THRESHOLD = 5;
		int LOW_FREQ_THRESHOLD = 1;
		int HIGH_TIME_BEFORE_ALERT = 5;
		int LOW_TIME_TO_RESET_ALERT = 30;

		ws = new WindSensor("MyWindSensor", "WindSensor Desciption", WINDSENSOR_CH, dom, HIGH_FREQ_THRESHOLD,
				LOW_FREQ_THRESHOLD, HIGH_TIME_BEFORE_ALERT, LOW_TIME_TO_RESET_ALERT);
		ws.registerListener(this);

		check(WindSensor.States.NORMAL, null, 0);

		simulateWind(HIGH_FREQ_THRESHOLD + 1, 500);
		check(States.HIGH, null, 0);

		simulateWind(HIGH_FREQ_THRESHOLD + 1, 4000);
		check(States.HIGH, EventType.SAFE, 4);

		simulateWind(HIGH_FREQ_THRESHOLD + 1, 1000);
		check(States.ALARM, EventType.ALARM, 6);

		simulateWind(HIGH_FREQ_THRESHOLD - 1, 1000);
		check(States.ALARM, EventType.ALARM, 7);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 2000);
		check(States.ALARM_BUT_LOW, EventType.ALARM, 9);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 27000);
		check(States.ALARM_BUT_LOW, EventType.ALARM, 36);

		simulateWind(LOW_FREQ_THRESHOLD - 1, 2000);
		check(States.NORMAL, EventType.SAFE, 38);
	}

	private void check(WindSensor.States stateExpected, IAlarmListener.EventType eventExpected, int nrEventsExpected) {
		Assert.assertEquals(stateExpected, ws.getState());
		Assert.assertEquals(eventExpected, lastEvent);
		Assert.assertEquals(nrEventsExpected, nrEvents);
	}

}

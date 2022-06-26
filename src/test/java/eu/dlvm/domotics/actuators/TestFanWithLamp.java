package eu.dlvm.domotics.actuators;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.Connector;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.iohardware.IHardware;
import org.junit.Assert;

public class TestFanWithLamp {

	public class Hardware extends BaseHardwareMock implements IHardware {
		public boolean lampStatus;
		public boolean fanStatus;

		@Override
		public void writeDigitalOutput(String ch, boolean value) throws IllegalArgumentException {
			if (ch.equals(FAN_OUT)) {
				fanStatus = value;
			} else if (ch.equals(LAMP_OUT)) {
				lampStatus = value;
			} else {
				Assert.fail();
			}
		}
	};

	private static final String FAN_OUT = Integer.toString(10);
	private static final String LAMP_OUT = Integer.toString(11);
	private Fan fan;
	private Lamp lamp;
	private Hardware hw;
	private long current;

	@Before
	public void init() {
		hw = new Hardware();
		DomoticLayout dom = new DomoticLayout();
		lamp = new Lamp("TestLamp", "TestLamp", false, LAMP_OUT, hw, dom);
		fan = new Fan("TestFanWithLamp", "TestFanWithLamp", FAN_OUT, hw, dom).overrideDelayToOnSec(5)
				.overrideDelayToOffSec(5)
				.overrideOnDurationSec(10);
		lamp.registerListener(new Connector(EventType.ON, fan, EventType.DELAY_ON, "Test_Lamp"));
		lamp.registerListener(new Connector(EventType.OFF, fan, EventType.DELAY_OFF, "Test_Lamp"));
		current = 0L;
	}

	private void assert_OFF() {
		Assert.assertEquals(FanStatemachine.States.OFF, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(!hw.fanStatus);
	}

	private void assert_ON() {
		Assert.assertEquals(FanStatemachine.States.ON, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.fanStatus);
	}

	private void assert_OFF_DELAY2ON() {
		Assert.assertEquals(FanStatemachine.States.OFF_DELAY2ON, fan.getState());
		Assert.assertFalse(fan.isOn());
		Assert.assertTrue(hw.lampStatus && !hw.fanStatus);
	}

	private void assert_ON_DELAY() {
		Assert.assertEquals(FanStatemachine.States.ON_DELAY, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.lampStatus && hw.fanStatus);
	}

	private void assert_ON_DELAY2OFF_lamp_on() {
		Assert.assertEquals(FanStatemachine.States.ON_DELAY2OFF, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(hw.lampStatus && hw.fanStatus);
	}

	private void assert_ON_DELAY2OFF_lamp_off() {
		Assert.assertEquals(FanStatemachine.States.ON_DELAY2OFF, fan.getState());
		Assert.assertTrue(fan.isOn());
		Assert.assertTrue(!hw.lampStatus && hw.fanStatus);
	}

	@Test
	public void manuallyTurnOnAndOffFan() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Switch on, manually,
		fan.toggle();
		fan.loop(current += 10);
		assert_ON();
		fan.loop(fan.getOnDurationSec() * 1000 - 20);
		assert_ON();
		fan.toggle();
		fan.loop(current += 10);
		assert_OFF();
	}

	@Test
	public void manuallyTurnOnFanAndLetItTimeout() {
		fan.loop(current);
		assert_OFF();
		fan.toggle();
		fan.loop(current += 10);
		assert_ON();
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10));
		assert_OFF();
		// Make sure it does not go on again...
		fan.loop(current += 10);
		fan.loop(current += fan.getDelayToOnSec() * 1000);
		fan.loop(current += 10);
		assert_OFF();
	}

	@Test
	public void lampLongEnoughOnToLetFanRun() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Turn off lamp, fan must still run
		lamp.off();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_off();
		// Now wait until fan should have stopped
		fan.loop(current += (fan.getOnDurationSec() * 1000 + 10));
		assert_OFF();
	}

	/* added after bug, when fan went on after lamp was off again */
	@Test
	public void lampNotLongEnoughOnForFanToRun() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Wait just before end of delay period, fan must still not run
		fan.loop(current += (fan.getDelayToOnSec() - 20));
		assert_OFF_DELAY2ON();
		// Turn off lamp, fan must not run
		lamp.off();
		fan.loop(current += 10);
		assert_OFF();
		// Now wait for running period, should still not run (of course not, but
		// this was a bug)
		fan.loop(current += (fan.getOnDurationSec() + 10));
		assert_OFF();
	}

	@Test
	public void stopFanManuallyWhileRunningWithLampOn() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.toggle();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Now set lamp off, should go to OFF
		lamp.off();
		fan.loop(current += 10);
		assert_OFF();
	}

	@Test
	public void toggleFanManuallyWhileRunningWithLampAlreadyOffShouldKeepRunning() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Lamp off
		lamp.off();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_off();
		// Toggle off
		fan.toggle();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_off();
	}

	@Test
	public void reallyOffWhenInOnDelay2OffShouldStopFanPermanently() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Lamp off
		lamp.off();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_off();
		// really off
		fan.reallyOff();
		fan.loop(current += 10);
		assert_OFF();
	}

	@Test
	public void togglWhenIn_OFF_DELAY2ON_HasNoEffect() {
		fan.loop(current);
		assert_OFF();
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Toggle
		fan.toggle();
		fan.loop(current += 10);
		assert_ON_DELAY();
	}

	@Test
	public void stopFanManuallyViaLongToggleWhileRunningWithLampOn() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Toggle off, but lamp still on, so goes immediately to Delayed Run
		fan.reallyOff();
		fan.loop(current += 10);
		assert_OFF();
	}

	@Test
	public void whenInDelayToOffAndLightGoesOnMustRemainSameState() {
		fan.loop(current);
		assert_OFF();
		fan.loop(current += 10);
		// Lamp on
		lamp.on();
		fan.loop(current += 10);
		assert_OFF_DELAY2ON();
		// Let lamp on long enough, so that fan goes on
		fan.loop(current += (fan.getDelayToOnSec() * 1000 + 10));
		assert_ON_DELAY();
		// Lamp off
		lamp.off();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_off();
		// Lamp on again
		lamp.on();
		fan.loop(current += 10);
		assert_ON_DELAY2OFF_lamp_on();
	}
}

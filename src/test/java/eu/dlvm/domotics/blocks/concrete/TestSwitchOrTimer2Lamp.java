package eu.dlvm.domotics.blocks.concrete;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.dlvm.domotics.actuators.Lamp;
import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.RememberedOutput;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable;
import eu.dlvm.domotics.connectors.Switch2OnOffToggle;
import eu.dlvm.domotics.connectors.SwitchClick2Toggle;
import eu.dlvm.domotics.controllers.Timer;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;
import junit.framework.Assert;

public class TestSwitchOrTimer2Lamp {
	public static class Hardware extends BaseHardwareMock implements IHardwareIO {
		private Map<LogCh, Boolean> inputs = new HashMap<LogCh, Boolean>();
		private Map<LogCh, Boolean> outputs = new HashMap<LogCh, Boolean>();

		@Override
		public void writeDigitalOutput(LogCh channel, boolean value) throws IllegalArgumentException {
			outputs.put(channel, value);
		}

		@Override
		public boolean readDigitalInput(LogCh channel) {
			return inputs.get(channel);
		}

		public void out(int ch, boolean val) {
			outputs.put(new LogCh(ch), val);
		}

		public boolean out(int ch) {
			return outputs.get(new LogCh(ch));
		}

		public void in(int ch, boolean val) {
			inputs.put(new LogCh(ch), val);
		}

		public boolean in(int ch) {
			return inputs.get(new LogCh(ch));
		}
	};

	private Domotic dom;
	private Hardware hw;
	private Switch sw1, sw2;
	private Lamp o1, o2;
	private long cur;

	@Before
	public void init() {
		cur = 0L;

		hw = new Hardware();
		hw.in(0, false);
		hw.in(1, false);
		hw.out(10, false);
		hw.out(11, false);

		Domotic.resetSingleton();
		dom = Domotic.singleton(hw);

		sw1 = new Switch("Switch1", "Switch1", new LogCh(0), dom);
		sw2 = new Switch("Switch2", "Switch2", new LogCh(1), dom);
		o1 = new Lamp("Lamp1", "Lamp1", new LogCh(10), dom);
		o2 = new Lamp("Lamp2", "Lamp2", new LogCh(11), dom);

		SwitchClick2Toggle sct1 = new SwitchClick2Toggle("sct1", "");
		sw1.registerListener(sct1);
		sct1.registerListener(o1);
		SwitchClick2Toggle sct2 = new SwitchClick2Toggle("sct2", "");
		sw2.registerListener(sct2);
		sct2.registerListener(o2);

		Switch2OnOffToggle s2allonoff = new Switch2OnOffToggle("allonoff", "", null);
		s2allonoff.map(ISwitchListener.ClickType.LONG, IOnOffToggleCapable.ActionType.OFF);
		s2allonoff.map(ISwitchListener.ClickType.DOUBLE, IOnOffToggleCapable.ActionType.ON);

		sw2.registerListener(s2allonoff);
		s2allonoff.registerListener(o1);
		s2allonoff.registerListener(o2);
	}

	@Test
	public void singleClick() throws InterruptedException {
		dom.initialize(new HashMap<String, RememberedOutput>(0));

		sw2.setSingleClickEnabled(true);
		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);

		// switch 1, single click
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(0, true);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(0, false);
		dom.loopOnce(cur += 10);// hier loopt het mis
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(false, hw.out(11));

		// switch 2, single click
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += (sw2.getDoubleClickTimeout() + 10));
		dom.loopOnce(cur += 1);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
	}

	@Test
	public void allOff() throws InterruptedException {
		dom.initialize(new HashMap<String, RememberedOutput>(0));

		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		// First put both on
		hw.in(0, true);
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(0, false);
		hw.in(1, false);
		dom.loopOnce(cur += 10); // detecteer single click, maar wacht nog op
									// dubbel click
		dom.loopOnce(cur += 60); // vermijd dubbel-klik detectie
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
		// Now, all off with Switch 2
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += (sw2.getLongClickTimeout() + 10));
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
	}

	@Test
	public void allOn() throws InterruptedException {
		dom.initialize(new HashMap<String, RememberedOutput>(0));

		sw2.setDoubleClickEnabled(true);
		sw2.setDoubleClickTimeout(50);
		sw2.setLongClickEnabled(true);
		sw2.setLongClickTimeout(100);
		Assert.assertEquals(false, hw.out(10));
		Assert.assertEquals(false, hw.out(11));
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		hw.in(1, false);
		dom.loopOnce(cur += 10);
		hw.in(1, true);
		dom.loopOnce(cur += 10);
		Assert.assertEquals(true, hw.out(10));
		Assert.assertEquals(true, hw.out(11));
	}

	@Test
	public void testTimer() {
		Timer t = new Timer("timer", "timer", dom);
		t.setOnTime(22, 0);
		t.setOffTime(7, 30);
		t.register(o1);
		dom.initialize(new HashMap<String, RememberedOutput>(0));

		assertFalse(t.isOn());
		assertFalse(o1.isOn());
		assertFalse(hw.out(10));
		assertFalse(hw.out(11));

		Calendar c = GregorianCalendar.getInstance();
		c.set(2013, 8, 2, 0, 0); // 2 september 2013, toen geschreven ;-)
		t.loop(c.getTimeInMillis(), 0);
		assertTrue(t.isOn());
		assertTrue(hw.out(10));

		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		t.loop(c.getTimeInMillis(), 0);
		assertFalse(t.isOn());
		assertFalse(hw.out(10));

		c.set(Calendar.HOUR_OF_DAY, 22);
		c.set(Calendar.MINUTE, 30);
		t.loop(c.getTimeInMillis(), 0);
		assertTrue(t.isOn());
		assertTrue(hw.out(10));
	}
}

package eu.dlvm.domotics.actuators;

import org.junit.Before;

import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.iohardware.IHardware;

public class TestScreensBase {

	public class Hardware extends BaseHardwareMock implements IHardware {
		public boolean dnRelais;
		public boolean upRelais;

		@Override
		public void writeDigitalOutput(String ch, boolean value) throws IllegalArgumentException {
			if (ch.equals("0"))
				dnRelais = value;
			else
				upRelais = value;
		}
	};

	protected static int DN = 0;
	protected static int UP = 1;
	protected Screen sr;
	protected Hardware hw;
	protected long cur;

	public TestScreensBase() {
		super();
	}

	protected void loop(long inc) {
		cur += inc;
		sr.loop(cur);
	}

	protected void loop() {
		loop(10);
	}

	@Before
	public void init() {
		hw = new Hardware();
		DomoticLayout dom = new DomoticLayout();
		sr = new Screen("TestScreens", "TestScreens", null, Integer.toString(DN), Integer.toString(UP), hw, dom);
		sr.setMotorUpPeriod(30);
		sr.setMotorDnPeriod(30);
		cur = 0L;
	}

}

package eu.dlvm.domotica.blocks;

import org.apache.log4j.Logger;

import junit.framework.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

public abstract class BaseHardwareMock implements IHardwareIO {

	static Logger log = Logger.getLogger(BaseHardwareMock.class);

	@Override
	public void writeDigitalOutput(LogCh channel, boolean value) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new NotImplementedException();
	}

	@Override
	public boolean readDigitalInput(LogCh channel) {
		Assert.fail("Must not come here for this test.");
		throw new NotImplementedException();
	}

	@Override
	public int readAnalogInput(LogCh channel) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new NotImplementedException();
	}

	@Override
	public void writeAnalogOutput(LogCh channel, int value) throws IllegalArgumentException {
		Assert.fail("Must not come here for this test.");
		throw new NotImplementedException();
	}

	@Override
	public void initialize() {
		log.trace("initialize() called, ignored because mock.");
	}

	@Override
	public void refreshInputs() {
		log.trace("refreshInputs() called, ignored because mock.");
	}

	@Override
	public void refreshOutputs() {
		log.trace("refreshOutputs() called, ignored because mock.");
	}

	@Override
	public void stop() {
		log.trace("stop() called, ignored because mock.");
	}

}

package eu.dlvm.domotics.factories;

import org.junit.Test;

import eu.dlvm.domotics.blocks.BaseHardwareMock;
import eu.dlvm.domotics.blocks.DomoticMock;
import org.junit.Assert;

public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoticMock dom = new DomoticMock();
		BaseHardwareMock hw = new BaseHardwareMock();
		XmlDomoticConfigurator.configure("src/test/resources/TestDomoticConfig.xml", hw, dom);
		Assert.assertEquals(15, dom.sensors.size());
		Assert.assertEquals(8, dom.actuators.size());
		Assert.assertEquals(2, dom.controllers.size());
	}
}

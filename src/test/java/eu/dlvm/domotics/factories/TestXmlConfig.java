package eu.dlvm.domotics.factories;

import org.junit.Test;

import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.blocks.BaseHardwareMock;
import org.junit.Assert;

public class TestXmlConfig {

	@Test
	public void testConfigure() {
		DomoticLayout dom = new DomoticLayout();
		BaseHardwareMock hw = new BaseHardwareMock();
		XmlDomoticConfigurator.configure("src/test/resources/TestDomoticConfig.xml", hw, dom);
		Assert.assertEquals(15, dom.getSensors().size());
		Assert.assertEquals(8, dom.getActuators().size());
		Assert.assertEquals(2, dom.getControllers().size());
	}
}

package eu.dlvm.domotica;

import junit.framework.Assert;

import org.junit.Test;

public class Misc {

	@Test
	public void promoteByteToInt() {
		byte b = (byte)255;
		int i = (int)b;
		System.out.println("b="+b+", i="+i);
		Assert.assertEquals(-1, i);
		
		b = (byte)127;
		i = (int)b;
		System.out.println("b="+b+", i="+i);
		Assert.assertEquals(127, i);
		
		b = (byte)128;
		i = (int)b;
		System.out.println("b=128"+", i="+i);
		Assert.assertEquals(-128, i);
	}
}

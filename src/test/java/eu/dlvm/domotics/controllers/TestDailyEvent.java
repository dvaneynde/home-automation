package eu.dlvm.domotics.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.DomoticLayout;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.events.IEventListener;

public class TestDailyEvent {

	// TODO invalid event

	public class EcoListener implements IEventListener {

		public EventType lastEvent = null;
		public int nrEvents = 0;

		@Override
		public String getName() {
			return "EcoListener";
		}

		@Override
		public void onEvent(Block source, EventType event) {
			lastEvent = event;	
			nrEvents++;	
		}
	}

	@Test
	public void testHappy() {
		EcoListener el = new EcoListener();

		DailyEvent t = new DailyEvent("dailyEvent", "DailyEvent test", new DomoticLayout());
		t.setTimeAndEvent(2, 0, EventType.ECO_ON);
		t.registerListener(el);
		assertNull(el.lastEvent);
		
		Calendar c = GregorianCalendar.getInstance();
		c.set(2023, 1, 28, 1, 0);
		t.loop(c.getTimeInMillis());
		assertNull(el.lastEvent);
		
		c.set(Calendar.HOUR_OF_DAY, 1);
		c.set(Calendar.MINUTE,30);
		t.loop(c.getTimeInMillis());
		assertNull(el.lastEvent);
		assertEquals(0, el.nrEvents);
		
		extracted(el, t, c, 1);
		extracted(el, t, c,2);
		
	}

	private void extracted(EcoListener el, DailyEvent t, Calendar c, int expectedNrEvents) {
		c.set(Calendar.HOUR_OF_DAY, 2);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND, 0);
		t.loop(c.getTimeInMillis());
		assertEquals(EventType.ECO_ON, el.lastEvent);
		assertEquals(expectedNrEvents, el.nrEvents);

		c.set(Calendar.HOUR_OF_DAY, 2);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND, 10);
		t.loop(c.getTimeInMillis());
		assertEquals(EventType.ECO_ON, el.lastEvent);
		assertEquals(expectedNrEvents, el.nrEvents);
		
		c.set(Calendar.HOUR_OF_DAY, 2);
		c.set(Calendar.MINUTE,1);
		c.set(Calendar.SECOND, 0);
		t.loop(c.getTimeInMillis());
		assertEquals(EventType.ECO_ON, el.lastEvent);
		assertEquals(expectedNrEvents, el.nrEvents);
		
		c.set(Calendar.HOUR_OF_DAY, 5);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND, 0);
		t.loop(c.getTimeInMillis());
		assertEquals(EventType.ECO_ON, el.lastEvent);
		assertEquals(expectedNrEvents, el.nrEvents);
	}

}

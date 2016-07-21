package eu.dlvm.domotics.controllers.newyear;

import eu.dlvm.domotics.actuators.DimmedLamp;

public class Sinus implements INewYearGadget {
	private int cycleTime;
	private double cycleStartRadians;
	private DimmedLamp lamp;

	public Sinus(DimmedLamp lamp, int cycleTimeMs, int cycleStartDegrees) {
		this.lamp = lamp;
		this.cycleTime = cycleTimeMs;
		this.cycleStartRadians = cycleStartDegrees * 2.0D * Math.PI / 360;
	}

	public double calcValue(long time) {
		double arg = time * 2.0D * Math.PI / cycleTime + cycleStartRadians;
		double val = (Math.sin(arg) / 2.0 + 0.5) * 0.7 + 0.3;	// tussen 30% en 100%
		// System.out.println("arg="+arg+", val="+val);
		return val;
	}

	@Override
	public void loop2(long time, GSstate state) {
		lamp.on((int) (calcValue(time) * 100));
	}

	public static void main(String[] args) {
		Sinus s = new Sinus(null, 2000, 270);
		for (long time = 0L; time <= 2000; time += 100) {
			System.out.println(Math.round(100 * s.calcValue(time)));
		}
	}
}

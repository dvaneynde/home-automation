package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * Data to update UI.
 * 
 * @author dirk
 *
 */
/**
 * The {@code UiInfoOnOff} class extends {@code UiInfo} and represents
 * a UI information object with an additional boolean state indicating
 * whether it is "on" or "off".
 * 
 * <p>This class provides constructors for initialization and getter/setter
 * methods to access and modify the "on" state.
 * 
 * @see UiInfo
 */
public class UiInfoOnOff extends UiInfo {
	private boolean on;

	public UiInfoOnOff() {
	}

	public UiInfoOnOff(Block block, String status, boolean on) {
		super(block, status);
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	@Override
	public String toString() {
		return "UiInfoOnOff [on=" + on + " [" + super.toString() + "]]";
	}

}

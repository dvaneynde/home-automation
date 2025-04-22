package eu.dlvm.domotics.service.uidata;

import eu.dlvm.domotics.base.Block;

/**
 * Data to update UI.
 * 
 * @author dirk
 *
 */
/**
 * The {@code UiInfoOnOffEco} class extends {@code UiInfoOnOff} to include an additional
 * property for eco mode. This class represents a UI information object with on/off
 * status and eco mode status.
 * 
 * <p>Fields:
 * <ul>
 *   <li>{@code eco} - A boolean indicating whether the eco mode is enabled or not.</li>
 * </ul>
 * 
 * @see UiInfoOnOff
 */
public class UiInfoOnOffEco extends UiInfoOnOff {
	private boolean eco;

	public UiInfoOnOffEco() {
	}

	public UiInfoOnOffEco(Block block, String status, boolean on, boolean eco) {
		super(block, status, on);
		this.setEco(eco);
	}

	public boolean isEco() {
		return eco;
	}

	public void setEco(boolean eco) {
		this.eco = eco;
	}

	@Override
	public String toString() {
		return "UiInfoOnOffEco [eco=" + eco + " [" + super.toString() + "]]";
	}
}

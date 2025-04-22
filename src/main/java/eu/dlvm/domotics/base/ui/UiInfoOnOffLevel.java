package eu.dlvm.domotics.base.ui;

import eu.dlvm.domotics.base.Block;

/**
 * The {@code UiInfoOnOffLevel} class extends {@code UiInfoOnOff} to include
 * an additional property for representing a level. This can be used to
 * represent devices or components that have an adjustable level in addition
 * to being turned on or off.
 * 
 * <p>It provides methods to get and set the level, as well as constructors
 * for creating instances with or without initial values.
 * 
 * @see UiInfoOnOff
 */
public class UiInfoOnOffLevel extends UiInfoOnOff{

	private int level;
	
	public UiInfoOnOffLevel() {
	}

	public UiInfoOnOffLevel(Block block, String status, boolean on, int level) {
		super(block, status, on);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "UiInfoOnOffLevel [level=" + level + " [" + super.toString() + "]]";
	}

}
package eu.dlvm.domotics.base.ui;

import eu.dlvm.domotics.base.Block;

/**
 * The {@code UiInfoLevel} class represents a UI information object that includes
 * a level and its associated range values (minimum, low, high, and maximum).
 * It extends the {@code UiInfo} class to provide additional level-specific details.
 * 
 * <p>This class is typically used to represent and manage level-based information
 * in a user interface, such as progress bars, sliders, or other range-based controls.</p>
 * 
 * <p>Fields:</p>
 * <ul>
 *   <li>{@code level} - The current level value.</li>
 *   <li>{@code min} - The minimum allowable value for the level.</li>
 *   <li>{@code low} - A threshold value indicating a low range.</li>
 *   <li>{@code high} - A threshold value indicating a high range.</li>
 *   <li>{@code max} - The maximum allowable value for the level.</li>
 * </ul>
 */
public class UiInfoLevel extends UiInfo {

	private int level, min, low, high, max;

	public UiInfoLevel() {
	}

	public UiInfoLevel(Block block, String status, int level, int min, int low, int high, int max) {
		super(block, status);
		this.level = level;
		this.min = min;
		this.low = low;
		this.high = high;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getLow() {
		return low;
	}

	public int getHigh() {
		return high;
	}

	public int getLevel() {
		return level;
	}

	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		return "UiInfoLevel [level=" + level + ", min=" + min + ", low=" + low + ", high=" + high + ", max=" + max
				+ " [" + super.toString() + "]]";
	}

}
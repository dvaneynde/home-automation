package eu.dlvm.domotics.events;

/**
 * <dl>
 * <dd>LEFT_CLICK</dd>
 * <dd>RIGHT_CLICK</dd>
 * <dd>LEFT_HOLD_DOWN</dd>
 * <dd>LEFT_RELEASED</dd>
 * <dd>RIGHT_HOLD_DOWN</dd>
 * <dd>RIGHT_RELEASED</dd>
 * <dd>LEFT_WITH_RIGHTCLICK</dd>
 * <dd>RIGHT_WITH_LEFTCLICK</dd>
 */
public enum EventType {
	ON("On"), OFF("Off"), TOGGLE("Toggle"), LIGHT_HIGH("LightHigh"), LIGHT_LOW("LightLow"), ALARM("Alarm"), SAFE("Safe"), 
	UP("Up"), DOWN("Down"), TOGGLE_UP("ToggleUp"), TOGGLE_DOWN("ToggleDown"), 
	SINGLE_CLICK("SingleClick"), DOUBLE_CLICK("DoubleClick"), LONG_CLICK("LongClick"), LEFT_CLICK("LeftClick"), RIGHT_CLICK("RightClick"), 
	LEFT_HOLD_DOWN("LeftHoldDown"), LEFT_RELEASED("LeftReleased"), RIGHT_HOLD_DOWN("RightHoldDown"), RIGHT_RELEASED("RightReleased"), LEFT_WITH_RIGHTCLICK("LeftWithRightClick"), RIGHT_WITH_LEFTCLICK("RightWithLeftClick"), 
	DELAY_ON("DelayOn"), DELAY_OFF("DelayOff"),
	ECO_ON("EcoOn"), ECO_OFF("EcoOff"), ECO_TOGGLE("EcoToggle");

	private String alias;

	EventType(String alias) {
		this.alias = alias;
	}

	public String getAlias() { return alias; }
	
	public static EventType fromAlias(String alias) {
		for (EventType e : EventType.values()) 
			if (e.alias.equalsIgnoreCase(alias))
				return e;
		return null;
	}
}
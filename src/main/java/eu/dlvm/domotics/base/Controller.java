package eu.dlvm.domotics.base;

import eu.dlvm.domotics.events.IEventListener;

/**
 * Controller drive Actuators or other Controllers, like Sensors do, but without
 * access to hardware.
 */
public abstract class Controller extends Block implements IDomoticLoop, IEventListener {

	public Controller(String name, String description, String ui, DomoticLayout layout) {
		super(name, description, ui);
		layout.addController(this);
	}

}

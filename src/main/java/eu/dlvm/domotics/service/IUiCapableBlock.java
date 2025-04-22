package eu.dlvm.domotics.service;

import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.events.EventType;
import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Blocks ({@link Block}) that can be shown and updated via UI.
 * 
 * @author dirk
 */
public interface IUiCapableBlock {

	/**
	 * Retrieves the UI information associated with this block.
	 * 
	 * @return a {@link UiInfo} object containing details for UI representation.
	 */
	public UiInfo getUiInfo();

	/**
	 * Retrieves the UI group associated with this block, and the position within the group. E.g.
	 * "Nutsruimtes:6" means group "Nutsruimtes", and the 7th position (0 based) therein.
	 * 
	 * @return the name of the UI group, a ':' and the position as a number
	 */
	public String getUiGroup();

	/**
	 * Update a Block through UI, if supported.
	 * 
	 * @param action See {@link EventType} for the action to be performed. Should be case insensitive.
	 */
	public void update(String action);
}

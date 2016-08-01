package eu.dlvm.domotics.connectors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Connector;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.connectors.IOnOffToggleCapable.ActionType;
import eu.dlvm.domotics.sensors.ISwitchListener;
import eu.dlvm.domotics.sensors.Switch;
import eu.dlvm.domotics.service.UiInfo;

/**
 * Keeps one or more mappings, each mapping maps one incoming
 * {@link ISwitchListener.ClickType} event to one
 * {@link IOnOffToggleCapable.ActionType} event. Whenever an event comes in, the
 * corresponding event is sent to all listeners.
 * 
 * @author dirkv
 * 
 *         TODO die mappers zijn eigenlijk low-level mechanismes; een all on/off
 *         is hoger niveau en kan makkelijk met <group> gemaakt worden, maar als
 *         je UI ziet... dus hoe kan dat algemeen opgelost? AllOnOff als apart
 *         element - maar wat dan? We lijken terug naar switchboards te gaan...
 *         Of Mapper heeft in- en output events, geen loop(), maar eventueel wel
 *         IUiCapable? En ook registreren bij Domotic, maar meestal niks
 *         bijhouden tenzij IUiCapable?
 */
public class Switch2OnOffToggle extends Connector implements ISwitchListener, IUiCapableBlock {

	static Logger log = LoggerFactory.getLogger(Switch2OnOffToggle.class);

	private Map<ClickType, IOnOffToggleCapable.ActionType> mappings;
	private Set<IOnOffToggleCapable> listeners = new HashSet<>();

	public Switch2OnOffToggle(String name, String description, String ui) {
		super(name, description, ui);
		mappings = new HashMap<>(3);
	}

	public void map(ClickType click, IOnOffToggleCapable.ActionType action) {
		mappings.put(click, action);
	}

	public void registerListener(IOnOffToggleCapable listener) {
		listeners.add(listener);
	}

	public void notifyListeners(ActionType action) {
		for (IOnOffToggleCapable l : listeners)
			l.onEvent(action);
	}

	@Override
	public void onEvent(Switch source, ClickType click) {
		IOnOffToggleCapable.ActionType action = mappings.get(click);
		if (action == null) {
			log.debug("Received event for which no action is registered. Name=" + getName() + ", event received=" + click + ", from switch=" + source);
			return;
		}
		notifyListeners(action);
	}

	@Override
	public UiInfo getUiInfo() {
		UiInfo bi = null;
		if (ui != null) {
			log.debug("getBlockInfo(), ui='" + ui + "'");
			bi = new UiInfo(this.getName(), "Switch", this.getDescription());
		}
		return bi;
	}

	// TODO slecht, want LONG is hardcoded, en hangt eigenlijk af van
	// configuratie...
	@Override
	public void update(String action) {
		if (action.equals("clicked")) {
			IOnOffToggleCapable.ActionType actionType = mappings.get(ClickType.LONG);
			notifyListeners(actionType);
		}
	}
}

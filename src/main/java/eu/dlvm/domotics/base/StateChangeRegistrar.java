package eu.dlvm.domotics.base;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateChangeRegistrar implements IStateChangeRegistrar {

    static Logger log = LoggerFactory.getLogger(StateChangeRegistrar.class);

    protected List<IStateChangedListener> stateChangeListeners = new LinkedList<>();
    private DomoticLayout layout;

    public StateChangeRegistrar(DomoticLayout layout) {
        this.layout = layout;
    }

    @Override
    public void addStateChangedListener(IStateChangedListener updator) {
        stateChangeListeners.add(updator);
        log.info("Add new UI updator id=" + updator.getId());
    }

    @Override
    public void removeStateChangedListener(IStateChangedListener updator) {
        boolean removed = stateChangeListeners.remove(updator);
        log.info("Removing updator id=" + updator.getId() + " (listener was found and thus removed: " + removed + ")");
    }

    public List<IStateChangedListener> getStateChangeListeners() {
        return stateChangeListeners;
    }

    public IUiCapableBlock findUiCapable(String name) {
        for (IUiCapableBlock ui : layout.getUiCapableBlocks()) {
            if (ui.getUiInfo().getName().equals(name))
                return ui;
        }
        return null;
    }
}

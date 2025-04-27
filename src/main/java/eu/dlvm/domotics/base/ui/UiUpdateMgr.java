package eu.dlvm.domotics.base.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.dlvm.domotics.base.Actuator;
import eu.dlvm.domotics.base.Block;
import eu.dlvm.domotics.base.Controller;
import eu.dlvm.domotics.base.DomoticLayout;

/**
 * Manages the UI update process for UI-capable blocks.
 * This class is responsible for registering such blocks once at start-up.
 * It also manages a list of UI updators that are responsible for sending updates to a UI (e.g., WebSocket clients).
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Register UI-capable blocks (Actuators and Controllers).</li>
 *   <li>Update the UI state at regular intervals.</li>
 *   <li>Manage a list of UI updators for sending updates to the UI.</li>
 * </ul>
 * 
 * @author Dirk
 */
public class UiUpdateMgr {

    static Logger log = LoggerFactory.getLogger(UiUpdateMgr.class);

    private List<IUiCapableBlock> uiBlocks;
    private List<IUiUpdator> uiUpdators;

    public UiUpdateMgr(DomoticLayout layout) {
        registerUiBlocks(layout);
        uiUpdators = new LinkedList<IUiUpdator>();
    }

    /**
     * @return all registered {@link Actuator} and {@link Controller} blocks that implement
     *         {@link IUiCapableBlock}, or those blocks registered explicitly...
     */
    public List<IUiCapableBlock> getUiCapableBlocks() {
        return uiBlocks;
    }

    // TODO perhaps use a map for this, since we need to find them by name
    public IUiCapableBlock findUiCapable(String name) {
        for (IUiCapableBlock ui : uiBlocks) {
            if (ui.getUiInfo().getName().equals(name))
                return ui;
        }
        return null;
    }

    public List<IUiUpdator> getUiUpdators() {
        return uiUpdators;
    }

    public void addUiUpdator(IUiUpdator uiUpdator) {
        uiUpdators.add(uiUpdator);
    }

    public void removeUiUpdator(IUiUpdator uiUpdator) {
        uiUpdators.remove(uiUpdator);
    }

    public void UpdateUis(long loopSequence) {
        // FIXME should be async in separate thread, since might take longer than 20 ms... and with timeout
        // perhaps?
        if (loopSequence % 10 == 0) {
            long startTimeWs = System.currentTimeMillis();
            List<UiInfo> uiInfos = buildUiInfos();
            for (IUiUpdator uiUpdator : getUiUpdators()) {
                uiUpdator.updateUi(uiInfos);
            }
            // TODO perhaps use a thread pool for this, since it might take a while to update all UIs (copilot)
            long tookMs = System.currentTimeMillis() - startTimeWs;
            if (tookMs >= 20)
                log.warn("Updating websockets took more than 19 ms! It took " + tookMs + " ms.");
        }
    }

    public List<UiInfo> buildUiInfos() {
        List<UiInfo> uiInfos = new ArrayList<>();
        for (IUiCapableBlock ui : uiBlocks) {
            if (ui.getUiGroup() == null)
            // TODO hack? notVisible:bool?
                continue;
            uiInfos.add(ui.getUiInfo());
        }
        return uiInfos;
    }

     
    private void registerUiBlocks(DomoticLayout layout) {
        uiBlocks = new ArrayList<IUiCapableBlock>(64);
        for (Block b : layout.getSensors())
            registerIfUiCapable(b);
        for (Block b : layout.getControllers())
            registerIfUiCapable(b);
        for (Block b : layout.getActuators())
            registerIfUiCapable(b);
    }

    private void registerIfUiCapable(Block b) {
        if (b instanceof IUiCapableBlock) {
            IUiCapableBlock uiblock0 = ((IUiCapableBlock) b);

            if (uiblock0.getUiInfo() == null) {
                log.warn("Not adding UI info for " + ((Block) uiblock0).getName()
                        + ". BlockInfo is null - is a bug, refactor code.");
                return;
            }
            for (IUiCapableBlock uiblock : uiBlocks) {
                if (uiblock.getUiInfo().getName().equals(uiblock0.getUiInfo().getName())) {
                    log.warn("addUiCapableBlock(): incoming UiCapable '" + uiblock0.getUiInfo().getName()
                            + "' already registered - ignored.");
                    return;
                }
            }
            uiBlocks.add(uiblock0);
            log.debug("Add UiCapableBlock " + uiblock0.getUiInfo().getName());
        }
    }
}


package eu.dlvm.domotics.base;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.dlvm.domotics.service.IUiCapableBlock;

public class DomoticLayout implements IDomoticLayoutBuilder {

    static Logger log = LoggerFactory.getLogger(DomoticLayout.class);

    private List<Sensor> sensors = new ArrayList<Sensor>(64);
    private List<Actuator> actuators = new ArrayList<Actuator>(64);
    private List<Controller> controllers = new ArrayList<Controller>(64);
    private List<IUiCapableBlock> uiblocks = null;


    /**
     * Add Sensor to loop set (see {@link #loopOnceAllBlocks(long)}.
     * 
     * @param domotic TODO
     * @param sensor
     *                Added, if not already present. Each Sensor can be present no
     *                more than once.
     */
    public void addSensor(Sensor sensor) {
        if (sensors.contains(sensor)) {
            Domotic.log.warn("Sensor already added, ignored: " + sensor);
            assert (false);
            return;
        }
        sensors.add(sensor);
        // log.info("Add sensor '" + sensor.getName()+"' - "+sensor.toString());
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * Add Actuator to loop set (see {@link #loopOnceAllBlocks(long)}.
     * 
     * @param actuator
     *                 Added, if not already present. Each Actuator can be present
     *                 no more than once.
     */
    public void addActuator(Actuator actuator) {
        if (actuators.contains(actuator)) {
            log.warn("Actuator already added, ignored: " + actuator);
            assert (false);
            return;
        }
        actuators.add(actuator);
        // log.info("Add actuator '" + actuator.getName()+"' - "+actuator.toString());
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public void addController(Controller controller) {
        if (controllers.contains(controller)) {
            log.warn("Controller already added, ignored: " + controller);
            assert (false);
            return;
        }
        controllers.add(controller);
        // log.info("Add controller '" + controller.getName()+"' -
        // "+controller.toString());
    }

    public List<Controller> getControllers() {
        return controllers;
    }

    // ----------- UI Support -------------

    /**
     * @return all registered {@link Actuator} and {@link Controller} blocks
     *         that implement {@link IUiCapableBlock}, or those blocks
     *         registered explicitly...
     */
    public List<IUiCapableBlock> getUiCapableBlocks() {
        if (uiblocks == null)
            registerUiCapables();
        return uiblocks;
    }

    public IUiCapableBlock findUiCapable(String name) {
        for (IUiCapableBlock ui : getUiCapableBlocks()) {
            if (ui.getUiInfo().getName().equals(name))
                return ui;
        }
        return null;
    }


    private void registerUiCapables() {
        uiblocks = new ArrayList<IUiCapableBlock>(64);
        for (Block b : sensors)
            registerIfUiCapable(b);
        for (Block b : controllers)
            registerIfUiCapable(b);
        for (Block b : actuators)
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
            for (IUiCapableBlock uiblock : uiblocks) {
                if (uiblock.getUiInfo().getName().equals(uiblock0.getUiInfo().getName())) {
                    log.warn("addUiCapableBlock(): incoming UiCapable '" + uiblock0.getUiInfo().getName()
                            + "' already registered - ignored.");
                    return;
                }
            }
            uiblocks.add(uiblock0);
            log.debug("Add UiCapableBlock " + uiblock0.getUiInfo().getName());
        }
    }
}

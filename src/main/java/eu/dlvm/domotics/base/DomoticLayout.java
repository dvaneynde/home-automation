package eu.dlvm.domotics.base;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DomoticLayout is a container for sensors, actuators, and controllers.
 * It provides methods to add and retrieve these components.
 * 
 * <p>This class is part of the Domotic framework and is used to organize and manage
 * the various components that make up a domotic system.</p>
 * 
 * <p>Key Responsibilities:</p>
 * <ul>
 *   <li>Add and retrieve sensors, actuators, and controllers.</li>
 *   <li>Ensure that each component is unique within the layout.</li>
 * </ul>
 * 
 * @author Dirk
 */
public class DomoticLayout {

    static Logger log = LoggerFactory.getLogger(DomoticLayout.class);

    private List<Sensor> sensors = new ArrayList<Sensor>(64);
    private List<Actuator> actuators = new ArrayList<Actuator>(64);
    private List<Controller> controllers = new ArrayList<Controller>(64);


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

}

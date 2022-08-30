package eu.dlvm.domotics.base;

/**
 * Methods to register a {@link Block}.
 * <p>Typically passed to a {@link Block} subtype so that it can register itself in {@link DomoticLayout}.
 * 
 * @author dirk
 * 
 */
public interface IBlockRegistrar {

	/**
	 * Add Sensor. 
	 * 
	 * @param s
	 *          Added, if not already present. Each Sensor can be present no
	 *          more than once.
	 */
	void addSensor(Sensor s);

	/**
	 * Add Actuator. 
	 * 
	 * @param a
	 *          Added, if not already present. Each Actuator can be present no
	 *          more than once.
	 */
	void addActuator(Actuator a);

	/**
	 * Add Controller. 
	 * 
	 * @param a
	 *          Added, if not already present. Each Actuator can be present no
	 *          more than once.
	 */
	void addController(Controller a);
}

package eu.dlvm.domotics.base;

/**
 * Interface defining {@link #loopOnceAllBlocks(long)}.
 */
public interface ICanLoopAllBlocks {
     /**
      * This is what happens:
      * <ol>
      * <li>{@link IHardware#refreshInputs()} is called, so that hardware layer
      * inputs are refreshed.</li>
      * <li>All registered Sensors have their {@link Sensor#loop(long)} run to read
      * input and/or check timeouts etc. This typically triggers Controllers or
      * Actuators.</li>
      * <li>Then any registered Controllers have their {@link Controller#loop(long)}
      * executed, which typically updates Actuators.</li>
      * <li>Then any registered Actuators have their {@link Actuator#loop(long)}
      * executed, so they can update hardware output state.</li>
      * <li>{@link IHardware#refreshOutputs()} is called, so that hardware
      * layer outputs are updated.</li>
      * <li>Finally any {@link IStateChangedListener}s are called to update model
      * state of connected client UIs.
      * </ol>
      * 
      * @param currentTime
      *                    Current time at loopOnce invocation.
      */
     void loopOnceAllBlocks(long currentTime);
}

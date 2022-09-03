package eu.dlvm.domotics.controllers.gadgets;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of {@link IGadget} that will 'run' in parallel, until {@link #getDurationMs()} has passed.
 * 
 * @author dirk
 *
 */
public class GadgetSet {
	private int durationMs;
	private List<IGadget> gadgets = new ArrayList<>();

	/**
	 * Constructor
	 * @param durationMs Time this set of {@link IGadget}s will run.
	 */
	public GadgetSet(int durationMs) {
		this.durationMs = durationMs;
	}
	
	public int getDurationMs() {
		return durationMs;
	}

	public List<IGadget> getGadgets() {
		return gadgets;
	}

	/** See {@link IGadget}. */
	public void onBefore() {
		for (IGadget g : gadgets)
			g.onBefore();
	}

	/** See {@link IGadget}. */
	public void onBusy(long relativeTimeGadgetSet) {
		for (IGadget g : gadgets)
			g.onBusy(relativeTimeGadgetSet);
	}

	/** See {@link IGadget}. */
	public void onDone() {
		for (IGadget g : gadgets)
			g.onDone();
	}
}

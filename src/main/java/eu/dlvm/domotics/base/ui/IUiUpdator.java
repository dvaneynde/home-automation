package eu.dlvm.domotics.base.ui;

import java.util.List;

/**
 * Interface for classes that can update a UI. For example websockets.
 * 
 * @author dirk
 */
public interface IUiUpdator {

	/** Just to keep track in easy way of listeners. */
	public int getId();
	
	public void updateUi(List<UiInfo> uiInfos);
}

package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IStateChangedListener;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dlvm.domotics.service.uidata.UiInfo;

// TODO Is created whenever a websocket is created, so whenever a client connects. If multiple clients connnect at same time, multiple are created.
// TODO Make thread safe? Make loop()) synchronised? At least COUNT variable.
// FIXME Why is this a IStateChangedListener? Needs major refactoring...

/**
 * WebSocket implementation for updating the UI state in real-time.
 * This class listens for state changes and sends updates to connected WebSocket clients.
 * It implements the {@link IStateChangedListener} interface to receive state change notifications.
 * 
 * <p>Each instance of this class is associated with a WebSocket session and is responsible
 * for managing its lifecycle, including opening and closing the session, and sending updates
 * to the client.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Manage WebSocket session lifecycle (open, close).</li>
 *   <li>Listen for state changes and send serialized UI information to the client.</li>
 *   <li>Serialize UI state into JSON format using Jackson's {@link ObjectMapper}.</li>
 * </ul>
 * 
 * <p>Usage:</p>
 * <ol>
 *   <li>Create an instance of this class, passing a list of {@link IStateChangedListener} to register itself.</li>
 *   <li>When a WebSocket connection is established, the {@code onOpen} method is called to initialize the session.</li>
 *   <li>When the connection is closed, the {@code onClose} method is called to clean up resources.</li>
 *   <li>The {@code updateUi} method is triggered to send the latest UI state to the client.</li>
 * </ol>
 * 
 * <p>Thread Safety:</p>
 * This class is not thread-safe. Ensure proper synchronization if accessed from multiple threads.
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>{@link ObjectMapper} for JSON serialization.</li>
 *   <li>{@link Domotic} singleton for accessing UI-capable blocks.</li>
 *   <li>{@link Logger} for logging lifecycle events and errors.</li>
 * </ul>
 * 
 * @author Dirk
 */
@WebSocket
public class UiStateUpdatorSocket implements IStateChangedListener {

	private static final Logger LOG = LoggerFactory.getLogger(UiStateUpdatorSocket.class);
	private static int COUNT = 0;
	private ObjectMapper objectMapper;
	private int id;
	private List<IStateChangedListener>  stateChangeListeners;
	private Session savedSession;

	public UiStateUpdatorSocket(List<IStateChangedListener>  stateChangeListeners) {
		this.stateChangeListeners = stateChangeListeners;
		this.objectMapper= new ObjectMapper();
		this.id = COUNT++;
		LOG.debug("Created UiStateUpdatorSocket, id=" + id);
	}

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.savedSession = session;
		stateChangeListeners.add(this);
		LOG.debug("Opened websocket session (id=" + id + ") for remote " + this.savedSession.getRemoteAddress());
	}

	@OnWebSocketClose
	public void onClose(int closeCode, String closeReasonPhrase) {
		this.savedSession = null;
		stateChangeListeners.remove(this);
		LOG.debug("Closed websocket session (id=" + id + "), reason=" + closeReasonPhrase);
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void updateUi() {
		LOG.debug("updateUI called on websocket id=" + id + ", session=" + savedSession);
		if (savedSession == null)
			return;
		try {
			String json = objectMapper.writeValueAsString(createUiInfos());
			savedSession.getRemote().sendString(json);
		} catch (Exception e) {
			LOG.warn("Cannot send state to client. Perhaps race condition, i.e. closed in parallel to update?", e);
		}
	}

	private List<UiInfo> createUiInfos() {
		List<UiInfo> uiInfos = new ArrayList<>();
		for (IUiCapableBlock ui : Domotic.singleton().getLayout().getUiCapableBlocks()) {
			if (ui.getUiGroup() == null)
				continue;
			uiInfos.add(ui.getUiInfo());
		}
		return uiInfos;
	}
}

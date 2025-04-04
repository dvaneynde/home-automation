package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUiCapableBlock;
import eu.dlvm.domotics.service.uidata.UiInfo;

@Singleton
@Path("")
public class RestService {

	private static Logger Log = LoggerFactory.getLogger(RestService.class);
	private static int countInstances = 0;
	private QuickieService qSvc;

	public RestService() {
		countInstances++;
		Log.info("Count instances: " + countInstances);
		qSvc = new QuickieService();
	}

	@Path("health")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String heatlh() {
		return "{'health':'ok'}";
	}

	@Path("shutdown")
	@GET
	public void shutdown() {
		Domotic.singleton().requestStop();
		Log.info("Shutdown of domotic requested.");
	}

	@Path("statuses")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public List<UiInfo> listActuators() {
		List<UiInfo> list = new ArrayList<>();
		try {
			for (IUiCapableBlock a : Domotic.singleton().getLayout().getUiCapableBlocks()) {
				UiInfo aj = a.getUiInfo();
				if (aj != null)
					list.add(aj);
			}
		} catch (Throwable e) {
			Log.warn("listActuators() failed", e);
			list.clear();
		}
		Log.debug("listActuators() returns: " + list);
		return list;
	}

	@Path("statuses/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public UiInfo getActuatorStatus(@PathParam("name") String name) {
		try {
			IUiCapableBlock uiCapable = Domotic.singleton().getLayout().findUiCapable(name);
			if (uiCapable == null) {
				Log.debug("getActuatorStatus() cannot find actuator with name: " + name);
				return null;
			} else {
				UiInfo info = uiCapable.getUiInfo();
				Log.debug("getActuatorStatus() returns: " + info);
				return info;
			}
		} catch (Throwable e) {
			Log.warn("getActuatorStatus() failed with name: "+name, e);
			return null;
		}
	}

	/**
	 * @param name
	 * @param action
	 *               on, off or integer which is level
	 */
	@Path("actuators/{name}/{action}")
	@POST
	public void updateActuator(@PathParam("name") String name, @PathParam("action") String action) {
		Log.info("Domotic API: got update actuator '" + name + "' action='" + action + "' (POST)");
		IUiCapableBlock act = Domotic.singleton().getLayout().findUiCapable(name);
		if (act == null) {
			// TODO iets terugsturen?
			Log.warn("updateActuator(): could not find actuator " + name);
		} else {
			act.update(action);
		}
	}

	@Path("quickies")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String quickies() {
		return qSvc.listQuickyNamesNewlineSeparated();
	}

	@Path("quick/{name}")
	@GET
	public void quick(@PathParam("name") String name) {
		Log.info("Domotic API: got quickie '" + name + "'");
		Quickie q = qSvc.find(name);
		if (q != null) {
			for (Quickie.KeyVal kv : q.actions) {
				updateActuator(kv.key, kv.val);
			}
		} else
			Log.warn("Domotic API: quickie '" + name + "' not found.");
	}
}

package eu.dlvm.domotics;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.dlvm.domotics.server.ServiceServer;
import io.logz.guice.jersey.JerseyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dlvm.domotics.base.Domotic;

/**
 * Domotic system main entry point.
 *
 * @author Dirk Vaneynde
 */
public class Main {

    public static final int DEFAULT_LOOP_TIME_MS = 20;

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Starts either domotic or hardware-console program. Optionally it starts
     * the hardware driver, or connects to an already running one.
     *
     * @param args TODO https://stackoverflow.com/questions/19203128/how-to-include-command-line-parameters-in-the-injection-process
     */
    public static void main(String[] args) {
        DomConfig cfg;
        try {
            cfg = new DomConfig(args);

            ServiceServer server = null;
            if (htmlRootFile != null) {
                server = new ServiceServer(htmlRootFile);
                server.start(this);
            } else
                log.warn("HTTP server not started as there is no html root file given.");

            /*
             Injector injector = Guice.createInjector(
        new ConfigModule(config),
        new YourOtherModule(),
        new YetAnotherModule());

    injector.getInstance(YourApplication.class).run();
    */
            if (cfg.domotic) {
                Injector injector = Guice.createInjector(
                        new DomConfigModule(cfg), new BasicModule(), new JerseyModule());
                Domotic dom = injector.getInstance(Domotic.class);
                dom.runDomotic(cfg.looptime, cfg.path2Driver, cfg.htmlRootFile);
            } else
                new HwConsoleRunner().run(cfg.hwCfgFile, cfg.hostname, cfg.port, cfg.path2Driver);
            log.info("ENDED normally Domotic system.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            usage();
        }
    }

    private static void usage() {
        System.out.println("Usage:\ttwo options:\n\t"
                + Main.class.getSimpleName() + " domo [-s] [-r] [-d path2Driver] [-t looptime] [-h hostname] [-p port] [-w webapproot] -b blocks-config-file -c hardware-config-file\n\t"
                + Main.class.getSimpleName() + " hw [-d path2Driver] [-h hostname] [-p port] -c hardware-config-file"
                + "\n\twhere:"
                + "\n\t-s simulate hardware driver (domotic only, for testing and development)"
                + "\n\t-d path to driver, if it needs to be started and managed by this program"
                + "\n\t-t time between loops, in ms; defaults to " + DEFAULT_LOOP_TIME_MS + " ms."
                + "\n\t-h hostname of hardware driver; incompatible with -d"
                + "\n\t-p port of hardware driver; incompatible with -d"
                + "\n\t-w path of directory with webapp (where index.html is located)"
                + "\n\t-b domotic blocks xml configuration file"
                + "\n\t-c hardware xml configuration file"
                + "\nTo configure logging externally, use 'java -Dlogback.configurationFile=/path/to/config.xml ...' or system env variable.\n");
        System.exit(2);
    }
}

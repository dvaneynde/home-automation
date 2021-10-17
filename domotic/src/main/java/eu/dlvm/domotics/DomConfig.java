package eu.dlvm.domotics;

import java.io.File;

public class DomConfig {
    public int looptime = Main.DEFAULT_LOOP_TIME_MS;
    public boolean domotic = false;
    public boolean simulation = false;
    public String path2Driver = null;
    public String blocksCfgFile = null;
    public String hwCfgFile = null;
    public String hostname = "localhost";
    public int port = 4444;
    public File htmlRootFile = null;

    public DomConfig(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("No arguments given.");
        }
        if (args[0].equalsIgnoreCase("domo"))
            domotic = true;
        else if (!args[0].equalsIgnoreCase("hw"))
            throw new RuntimeException("Need either \"domo\" or \"hw\" as first parameter.");
        int i = 1;
        while (i < args.length) {
            if (args[i].equals("-t")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -t needs an argument.");
                looptime = Integer.valueOf(args[i++]);
            } else if (args[i].equals("-d")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -d needs an argument.");
                path2Driver = args[i++];
            } else if (args[i].equals("-s")) {
                i++;
                simulation = true;
            } else if (args[i].equals("-b")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -b needs an argument.");
                blocksCfgFile = args[i++];
            } else if (args[i].equals("-c")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -c needs an argument.");
                hwCfgFile = args[i++];
            } else if (args[i].equals("-h")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -h needs an argument.");
                hostname = args[i++];
            } else if (args[i].equals("-p")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -p needs an argument.");
                port = Integer.parseInt(args[i++]);
            } else if (args[i].equals("-w")) {
                if (++i >= args.length)
                    throw new RuntimeException("Option -w needs an argument.");
                htmlRootFile = new File(args[i++]);
            } else {
                throw new RuntimeException("Argument error. Failed on " + args[i]);
            }
        }

        if (hwCfgFile == null) {
            throw new RuntimeException("Need hardware configuration file.");
        }
        if (domotic && (blocksCfgFile == null)) {
            throw new RuntimeException("Both blocks-config-file and hardware-config-file must be specified for domotic system.");
        }
        if (simulation && path2Driver != null) {
            throw new RuntimeException("Cannot have both simulation and a path to the hardware driver.");
        }
    }
}

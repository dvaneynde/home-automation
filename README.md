# DIY home automation - Main Java Program

## How to run

```bash
# For help
$ java -jar domotica-1.0-jar-with-dependencies.jar -h

Usage:	Main domo [-s] [-r] [-d path2Driver] [-t looptime] [-h hostname] [-p port] [-w webapproot] -b blocks-config-file -c hardware-config-file
	Main hw [-d path2Driver] [-h hostname] [-p port] -c hardware-config-file
	-s simulate hardware driver (domotic only, for testing and development)
	-d path to driver, if it needs to be started and managed by this program
	-t time between loops, in ms; defaults to 20 ms.
	-h hostname of hardware driver; incompatible with -d
	-p port of hardware driver; incompatible with -d	
    -w path of directory with webapp (where index.html is located)
	-b domotic blocks xml configuration file
	-c hardware xml configuration file
To configure logging externally, use 'java -Dlogback.configurationFile=/path/to/config.xml ...' or system env variable.
Domotica Main
```


## Run in simulation mode
Make sure subfolder `static` has the javascript (from elm) code.

```bash
$ java -jar domotica-1.0-jar-with-dependencies.jar -Dlogback.configurationFile=src/main/resources/logback-dev.xml \
    domo -s -c DiamondBoardsConfig.xml -b DomoticConfig.xml -w static
```

## TODO

1. Listen on port 80, not 8080.
2. REST API, herwerken, b.v. POST etc.
3. `systemd` ipv `init.d`.
4. Health check via REST api, bovenop interne check op loop().
5. HardwareIO heeft al exit 1 !!!
6. xml resources in src/main/resources, as default
7. logging config in src/main/resources, as default
8. logging.properties ook op bordje, en -D bij domotic.sh


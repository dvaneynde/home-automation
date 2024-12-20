<span style="font-family:Arial; font-size:3em;">DIY Home Automation</span>

> Note: git log shows too many entries, repository was once an everything-combined repo, which was not a good idea.

# Contact & Licensing
- Author: Dirk Vaneynde
- Contact: dirk@dlvmechanografie.eu
- License: [MIT License](./LICENSE.txt)

# Overview

Home automation system built up from common hardware and custom software.

A functional and technical overview is in [Design](./DESIGN.md), Structured a Software Architecture Document. 

Some historical evolutions (and more photos) are in [History](./HISTORY.md).

# Projects

There are multiple projects, each one in its own sub-folder with its own README:

1. [home-automation](./README.md) : (this repository) the main program, backend in Java
2. [hwdriver](../hwdriver/README.md) : the C program talking to the hardware
3. [elm-ui](../elm-ui/README.md) : the web UI
4. [azimuth](../azimuth/README.md) : library to calculate azimuth,  written in Scala
5. [home-automation/deployment](./deployment/README.md) : automated deployment of increments, including hwdriver; also describes first time setup

# Build and Deploy

See [Deployment](./deployment/README.md).

# How to run

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


To run in simulation mode, make sure subfolder `static` has the javascript (from elm) code. Then:

```bash
$ java -Dlogback.configurationFile=src/main/resources/logback-dev.xml -jar target/domotica-1.0-jar-with-dependencies.jar  \
    domo -s -c DiamondBoardsConfig.xml -b DomoticConfig.xml -w static
```


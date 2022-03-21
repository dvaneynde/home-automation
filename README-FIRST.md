# DIY home automation

> Note: git log shows too many entries, repository was once an everything-combined repo, which was not a good idea.

## Contact & Licensing
- Author: Dirk Vaneynde
- Contact: dirk@dlvmechanografie.eu
- License: [MIT License](./LICENSE.txt)

## Overview

Home automation system built up from common hardware. 

> **Note:** It only handles lights and sun screens, not wall sockets or heating or other fancy stuff. 

The primary goal of this system is for me, as a software engineer, to keep my skills up to date, have some fun and sometimes help convince recruiters to hire me ;-) There are much more feature rich open-source systems available elsewhere.

Here is a view of the hardware I currently use.

![The System](images/domo-v2-b.png)

Basically it uses Advantech and Diamond hardware with digital and analog inputs and outputs, connected to switches, lamps, voltage controlled dimmers or a weather station that measures wind and light.

There are two inputs: the common switches and an [elm](https://elm-lang.org) based UI.

More details are in [Design](./DESIGN.md), and some historical evolutions (and more photos) are in [History](./HISTORY.md).

## Projects

There are multiple projects, each one in its own sub-folder with its own README:

1. [home-automation](./README.md) : the main program, java based
2. [hwdriver](../hwdriver/README.md) : the C program talking to the hardware
3. [elm-ui](../elm-ui/README.md) : the web UI
5. [azimuth](../azimuth/README.md) : library to calculated azimuth, scala based
4. [home-automation/deployment](./deployment/README.md) : automated deployment of increments, including hwdriver; also first time setup

## Build and Deploy

See [Deployment](./deployment/README.md).
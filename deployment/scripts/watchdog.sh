#!/bin/bash

DOMDIR=/home/dirk/domotic
PIDFILE=$DOMDIR/domotic.pid
INITSCRIPT=/etc/init.d/domotic.sh

checkprocess() {
	if [[ $(date +%M) -eq 0 ]] ; then 
		datetext=$(date +"%Y-%m-%d %H:%M")
		echo -e "${datetext}  Watchdog is running. This message is shown only once per hour."; 
	fi
	if [[ -f $1 ]]; then
		pid=$(cat $1)
		ps -p $pid >/dev/null
		if [ $? -ne 0 ]
		then
			echo "Found pid file $1 but not its process ${pid}. Will restart domotic after 10 sec sleep."
			sleep 10
			echo "Restart..."
			sudo $INITSCRIPT restart
		fi
	else echo "WARNING could not find pid file $1"
	fi
}

checkprocess $PIDFILE

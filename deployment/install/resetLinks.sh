if [[ $# -ne 1 ]]; then echo "Specify new folder."; exit 2; fi
NEWDIR=$1
if [ ! -d $NEWDIR ]; then echo "Does not exist: $NEWDIR"; exit 2; fi

rm -f domotic.jar
rm -f hwdriver
rm -f DiamondBoardsConfig.xml DomoticConfig.xml
ln -s $NEWDIR/domotic*.jar domotic.jar
ln -s $NEWDIR/hwdriver hwdriver
ln -s $NEWDIR/DiamondBoardsConfig.xml DiamondBoardsConfig.xml
ln -s $NEWDIR/DomoticConfig.xml DomoticConfig.xml
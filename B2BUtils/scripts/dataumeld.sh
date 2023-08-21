#!/bin/bash
###############################################################################
#  Author       : Andreas Gaffke d&d <extern.gaffke_andreas@allianz.com>
#-----------------------------------------------------------------------------
#  PROJECT        : Allianz File Transfer / SFG
#
#  SYNTAX         : dataumeld.sh [b2bSftAdmin.sh options] <input file> <outputfile>
#
#
#  DESCRIPTION    : wrapper for b2bSftAdmin.sh DATAU Meldesatz loading.
#
#  RETURN VALUE   : 0 = ok, not 0 = error
###############################################################################

# Trace:
#set -x

if [ $# -lt 2 ]; then
  echo "usage $0 [b2bSftAdmin.sh options] <input file> <outputfile>"
  exit 99
fi
INFILE=${@:$(($# - 1)):1}
OUTFILE=${@:$(($#)):1}
set -- "${@:1:$(($# - 2))}"

MYDIR="/SFG_DATA/sfg_data/b2biutils"
MAINCLASS="de.denkunddachte.b2biutil.api.AzSftFullUtil"
JAVA_AGENT="org.eclipse.persistence.jpa-3.0.3.jar"
JAVA="/opt/cd/sfg/SFG/jdk/jre/bin/java"
ECLIPSELINK_JAR="$MYDIR/lib/$JAVA_AGENT"

if [ ! -f "$ECLIPSELINK_JAR" ]; then
  echo "Could not find $JAVA_AGENT in $MYDIR "
  exit 1
fi
export LANG=en_US.UTF-8

"$JAVA" -Dddutils.debug=false -javaagent:$ECLIPSELINK_JAR -cp $(ls -1t $MYDIR/lib/B2BiUtils*.jar|head -1) $MAINCLASS "$@" -i $INFILE
RC=$?

if [ $RC == 0 ]; then
  cp "$INFILE" "$OUTFILE"
fi
exit $RC

#------------------------------------ EOF --------------------------------------

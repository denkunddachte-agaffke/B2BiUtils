#!/bin/bash
#
#echo "ARGS: $@"
#set -x
msys_path() {
	local path="$1"
	local out p
	local wd=$(pwd)
	while read p; do
		if [ -d "$p" ] && cd "$p"; then
			out="${out:+$out;}$(pwd -W)"
		elif [ -f "$p" ]; then
			cd $(dirname "$p")
			out="${out:+$out;}$(pwd -W)/"$(basename "$p")
		fi
		cd "$wd"
	done < <(echo "$path" | sed -e 's/:/\n/g')
	echo $out
}

MYDIR=$(dirname "$(readlink -f $0)")
OLDWD="$(pwd)"
cd "$MYDIR"

NAME=B2BUtils
MAINCLASS="de.denkunddachte.utils.SIExportFile"
while [ 1 ]
do
    if [ "$1" == "-class" ]
    then
        MAINCLASS="$2"
        shift 2
    elif [ "$1" == "-debug" ]
    then
        JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=18000,suspend=y"
        shift
    else
        break
    fi
done
classfile="$(echo $MAINCLASS | tr -s '.' '/').class"

JAVA_OPTS="-Doracle.jdbc.J2EE13Compliant=true -Djava.awt.headless=true"

if [ -n "$JRE_HOME" ]
then
	JAVA="$JRE_HOME/bin/java"
elif [ -n "$JAVA_HOME" ]
then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA=java
fi

if [ -f "target/classes/$classfile" ]
then
	CP="target/classes"
else
	CP=$(ls -1rt "$NAME"*.jar | tail -1 2>/dev/null)
fi

for libdir in target/lib lib
do
	if [ -d "$libdir" ]
	then
		for f in $libdir/*.jar
		do
			CP=$CP:$f
		done
	fi
done

# IBM SDK used TLSv1.0 as default which causes connections to MSSQL to fail:
export IBM_JAVA_OPTIONS="-Dcom.ibm.jsse2.overrideDefaultTLS=true"

case $(uname -o) in
	Cygwin*) 
		CP=$(cygpath -m -p $CP)
		JAVA_OPTS="-Dworkdir=$(cygpath -m $OLDWD) -Dinstalldir=$(cygpath -m $MYDIR) $JAVA_OPTS"
		;;
	Msys*) 
        CP=$(msys_path $CP)
        JAVA_OPTS="-Dworkdir=$(msys_path $OLDWD) -Dinstalldir=$(msys_path $MYDIR) $JAVA_OPTS"
        ;;
	*) 	JAVA_OPTS="-Dworkdir=$OLDWD -Dinstalldir=$MYDIR $JAVA_OPTS"
		;;
esac

umask 007

if [ -x "$(which tput 2>/dev/null)" ]
then
	#	LINES=$(tput lines)
	COLUMNS=$(tput cols)
elif [ -z "$COLUMNS" ]
then
	kill -s WINCH $$
fi 
export LINES COLUMNS
export LANG=en_US.UTF-8

"$JAVA" $JAVA_OPTS -cp "$CP" $MAINCLASS "$@"
	 	 
cd "$OLDWD"
exit $?


#!/bin/bash

MYDIR=$(dirname $(readlink -f "$0"))
if [ $(basename $MYDIR) == "scripts" ]; then
  MYDIR=$(dirname $MYDIR)
fi

MAINCLASS="de.denkunddachte.b2biutil.api.AzSftFullUtil"
JAVA_AGENT="org.eclipse.persistence.jpa-3.0.3.jar"

if [ -n "$JRE_HOME" ]
then
  JAVA="$JRE_HOME/bin/java"
elif [ -n "$JAVA_HOME" ]
then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi
for dir in $MYDIR $HOME/.gradle/caches/modules-2/files-2.1 $HOME/.m2/repository; do
  [ -d "$dir" ] || continue
  ECLIPSELINK_JAR=$(find $dir -name "$JAVA_AGENT" 2>/dev/null|head -1)
  [ -n "$ECLIPSELINK_JAR" ] && break
done

if [ ! -f "$ECLIPSELINK_JAR" ]; then
  echo "Could not find $JAVA_AGENT in $MYDIR "
  exit 1
fi

export LANG=en_US.UTF-8

"$JAVA" -Dddutils.debug=false -javaagent:$ECLIPSELINK_JAR -cp $(find $MYDIR -name "B2BiUtils*.jar"|head -1) $MAINCLASS "$@"
exit $?


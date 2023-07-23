#!/bin/bash
MYDIR=$(dirname $(readlink -f "$0"))
if [ $(basename $MYDIR) == "scripts" ]; then
  MYDIR=$(dirname $MYDIR)
fi

MAINCLASS="de.denkunddachte.b2biutil.workflow.WorkflowUtil"
REFRESH_BP="AZ_REFRESH_PROPERTIES"

if [ -n "$JRE_HOME" ]
then
  JAVA="$JRE_HOME/bin/java"
elif [ -n "$JAVA_HOME" ]
then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

export LANG=en_US.UTF-8

"$JAVA" -Dddutils.debug=false -cp $(find $MYDIR -name "B2BiUtils*jar"|head -1) $MAINCLASS -E $REFRESH_BP
exit $?


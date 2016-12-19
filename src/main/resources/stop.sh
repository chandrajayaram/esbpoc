#!/bin/bash
set -e
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Stop script for spring boot application.  Spring profile must be passed
# as single (sole) argument to this script OR 'all' to kill all running
# instances. (e.g., ./start.sh prod or ./start.sh all)
#
# Environment/profile name from application.yml (as spring.profiles property)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JARPATH=$DIR/${project.build.finalName}.jar
JAR=$(basename $JARPATH)

# When this file exists, set its environment variables
if [ -f $DIR/.profile ] ; then
    source $DIR/.profile
fi

# Ensure we have a spring profile passed as argument or defined in environment
if [ -z "$SPRING_PROFILE" ]; then
    #
    # Require profile name to be passed on commandline as single sole argument
    #
    if [ $# -ne 1 ]; then
        echo
        echo "ERROR: Illegal number of parameters"
        echo "  Usage:"
        echo "   $0 [profile] or $0 all"
        echo
        echo "where [profile] is one of:"
        grep "# profile:" ${DIR}/application.yml | cut -d : -f 2
        echo
        exit -1;
    fi
    SPRING_PROFILE=$1
fi
echo "Using profile [$SPRING_PROFILE]"

#
# Kill running server
#
# params
#   1: JAR filename of spring-boot app
#   2: spring-boot profile defined in application.yml
#
function killServer() {
    local _JAR=$1
    local _PROFILE=$2

    local _PIDFILE="$DIR/.$_PROFILE.pid"

    if [ -f "$_PIDFILE" ] ; then
        invokeStop $_PIDFILE
    fi
}
#
# Kill ALL running server
#
# params
#   1: JAR filename of spring-boot app
#
function killAll() {
    local _JAR=$1

    for f in $DIR/.*.pid ; do
        if [ -f $f ] ; then
            invokeStop $f
        fi
    done
}
#
# Stops process, after some delay (wait) it'll kill -KILL to force stop
#
# params
#   1: file containing process id (PID)
#
function invokeStop(){
    if [ $# -ne 1 ]; then
        echo
        echo "ERROR: invokeStop() : Illegal number of parameters"
        exit -1;
    fi
    local _PIDFILE=$1

    # global PID var
    PID="`cat ${_PIDFILE}`"
    if ( checkRunning ); then
        echo "Stopping pid $PID"
        kill $PID | true > /dev/null

        finished=false
        seconds=0
        echo "INFO: Waiting at least 10 seconds for regular process termination of pid '$PID' : "
        while [ "$seconds" -le "10" ]; do
            if [ checkRunning ]; then
                sleep 1
                printf  "."
            else
                echo " TERMINATED"
                finished=true
                break
            fi
            seconds="`expr $seconds + 1`"
        done
        if [ $finished  ]; then
            RET=0
        else
            echo
            echo "INFO: Regular shutdown not successful,  sending SIGKILL to process"
            kill -KILL $PID
            RET="$?"
            echo "kill process returned $RET"
        fi
    elif [ -f "$_PIDFILE" ]; then
        echo "ERROR: No or outdated process id in '$_PIDFILE'"
        echo
        echo "INFO: Removing $_PIDFILE"
        RET=0
    else
        echo "Not running"
        RET=0
    fi
    rm -f "$_PIDFILE" >/dev/null 2>&1
    exit $RET
}
#
# Checks if process in global var $(PID) is running
# return:
#   0 : is running, else != 0, not running
#
function checkRunning(){
    RET="`ps -p "${PID}"|grep java`"
    if [ -n "$RET" ];then
        return 0;
    else
        return 1;
    fi
}


if [ $SPRING_PROFILE == "all" ] ; then
    killAll $JAR
else
    killServer $JAR $SPRING_PROFILE
fi



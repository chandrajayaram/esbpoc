#!/bin/bash
#set -e
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Startup script for spring boot application.  Spring profile must be passed
# as single (sole) argument to this script. (e.g., ./start.sh prod)
#
# Environment/profile name from application.yml (as spring.profiles property)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JAR=$DIR/${project.build.finalName}.jar
# true for nohup & call, false to allow CTRL-C this script to stop process
RUN_IN_BACKGROUND=true

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
        echo "ERROR: No Spring Profile specified.  set SPRING_PROFILE in .profile or pass as argument:"
        echo "  Usage:"
        echo "   $0 [profile]"
        echo
        echo "where [profile] is one of:"
        grep "# profile:" ${DIR}/application.yml | cut -d : -f 2
        echo
        exit -1;
    fi
    SPRING_PROFILE=$1
fi
echo "Using profile [$SPRING_PROFILE]"

# JVM options, e.g., for formatting of garbage collection logs
JAVA_OPTIONS+="-classpath $DIR -Xloggc:${DIR}/logs/gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps "

#
# Start spring-boot jar with configs yml properties file and spring profile
#
# params
#   1: JAR path and filename of spring-boot app
#   2: spring-boot profile defined in application.yml
function startSpringJAR() {
    local _JARPATH=$1
    local _PROFILE=$2

    local _JAR=$(dirname "${_JARPATH}")
    local _PID="."$_PROFILE".pid"
    local _JVM_OPTS="$JAVA_OPTIONS -Dspring.profiles.active=$_PROFILE -Dspring.config.location=$DIR/application.yml -jar $_JARPATH"

    echo "Starting $_JAR with profile $_PROFILE "

    #
    # The default search path for property file is:
    #   classpath:,classpath:/config,file:,file:config/
    # This is always used, irrespective of the value of spring.config.location.  In that way you can set up default
    # values for your application in application.properties and override it at runtime with a different file,
    # keeping the defaults.
    #
    pushd $DIR
    killServer $_JAR $_PROFILE
    if [ "$RUN_IN_BACKGROUND" == true ]; then
        if [ ! -d "logs" ]; then
            echo "Creating logs dir..."
            mkdir "logs"
        fi

        # nohup java $_JVM_OPTS > $DIR"/$_PROFILE.log" 2>&1 &
        nohup java $_JVM_OPTS > "logs/stdout.log" 2>&1 &
        echo $! > $_PID
    else
        java $_JVM_OPTS
    fi
    popd
}

#
# Kill running server
#
# params
#   1: JAR path and filename of spring-boot app
#   2: spring-boot profile defined in application.yml
function killServer() {
    local _JAR=$1
    local _PROFILE=$2
    local _PID="."$_PROFILE".pid"

    if [ -f "$_PID" ] ; then
        echo "killing PID " $(cat $DIR/$_PID)
        kill -9 $(cat $DIR/$_PID) > /dev/null 2>&1
        rm $DIR/$_PID
    fi
}


startSpringJAR $JAR $SPRING_PROFILE


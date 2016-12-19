###########################################################################
${project.name} : ${project.version} : ${project.timestamp} : ${project.builtBy}
===========================================================================

Service Gateway is the gateway app from the outside callers to internal services.


===========================================================================
Building
---------------------------------------------------------------------------
    System requirements:
        java jdk 1.8+, maven 3.3+

    This module has POM dependencies on libraries:
        caiso.camel.shared
        caiso.esb.common
    Ensure these are either built and in your local maven repository or available in s shared (i.e., Nexus) repo.

    Then run from shell in same dir as pom.xml
        mvn clean install


===========================================================================
Installation
---------------------------------------------------------------------------
    System requirements:
        none

    This is a Spring Boot application bundled as a jar, distributed as a zip file.  Simply unzip the file in its own directory.

    See below for configuration and running the application.


===========================================================================
Configuration
---------------------------------------------------------------------------
    System requirements:
        none

    There are 2 configuration files, one for logging and one for the application.

    There are also two scripts to start/stop the application that shouldn't need to be changed.

    -----------------------------------------------------------------------
    Logging: log4j2.xml
    ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        Unlike other/older applications, this uses log4j version 2.  It's base configuration is in log4j2.xml should sufficient without edits.

        see https://logging.apache.org/log4j/2.x/manual/configuration.html

    -----------------------------------------------------------------------
    Application configuration: application.yml
    ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        The yaml file utilizes spring profiles, multiline values and nested properties.
            see:
                http://www.yaml.org/
                http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-yaml

        The YAML file, among other things, defines the web server port the gateway listens on for inbound broadcast HTTP messages.


===========================================================================
Running
---------------------------------------------------------------------------
    System requirements to run:
        java 1.8+

    To start or stop the web server, execute the included scripts from any directory:
        <path-to-dist>/start.sh <profile>
            where <profile> is one of the spring profiles defined in application.yml

        <path-to-dist>/stop.sh <profile | all>
            where <profile> is one of the spring profiles defined in application.yml or pass 'all' to stop all running instances


===========================================================================
Running locally - dev
---------------------------------------------------------------------------
    To run completely 'local', you need the following installed and running:
        - ActiveMQ Broker
        - Cassandra

    There's a 'localDev' profile defined in application.yml that configures these.

    There's also a '.profile' in src/main/resources that's used by the start.sh to start the.  If you run this from within your IDE, include those "local environment-specific" variables defined in the .profile file.

    -----------------------------------------------------------------------
    Local ActiveMQ Broker
    ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        Running a local broker lets you test JMS traffic without effecting others in dev.

        To run a broker, start it from a shell prompt in the same directory as a pom.xml file.
        Simply run the following maven command:
            mvn org.apache.activemq.tooling:maven-activemq-plugin:5.7.0:run

    -----------------------------------------------------------------------
    Local Cassandra
    ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
        After installing Cassandra, you need to ensure your DB has the keyspace (aka schema) configured.

        see readme.txt in caiso.esb.common module in \cql directory


===========================================================================
Metrics
---------------------------------------------------------------------------
    This application uses Spring Actuator that exposes health and status metrics over an HTTP interface.

    The configuration for this in the .yml file is specified as in the below example (see .yml for actual values and profile overrides):
        # Spring Actuator/management port
        management:
          port: 50001


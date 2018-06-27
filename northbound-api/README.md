# Arqueopterix SDN controller Northbound API

This repository contains an OpenDaylight Nitrogen SR3 bundle that implements the Northbound REST API provided by the SDN controller developed for the Arqueopterix project. The API was developed using Swagger and is accessible in the following link (https://app.swaggerhub.com/apis/cgiraldo/arqueopterix-policy_enforcement/0.1)

<img src="/images/northboundapi_drawing.png" width="600">

**Figure 1: Arqueopterix SDN controller architecture.**

## Build instructions

To build this module a machine with the following requirements is necessary:
 * JDK 1.8+
 * Maven 3.5+

This module depends on the bundle OpenFlowQosService that provides access to modify OpenFlow rules in network devices. This module must be compiled and installed in the local Maven repository before attempting to build the Northbound API.

The following command compiles the code and installs the generated bundles to the local Maven repository:

```sh
mvn clean install -DskipTests
```
> The option _-DskipTests_ is used in order to prevent test execution until proper testing is implemented in this module.

## Running the controller

The compilation generates a full featured OpenDaylight controller including all the necessary bundles to provide access to OpenFlow network devices. The following commands are used to start the resulting OpenDaylight controller:

```sh
cd karaf/target/assembly/bin
./karaf
```
This will result in an screen similar to the following one:

```
Apache Karaf starting up. Press Enter to open the shell now...
100% [========================================================================]

Karaf started in 0s. Bundle stats: 11 active, 11 total

    ________                       ________                .__  .__       .__     __       
    \_____  \ ______   ____   ____ \______ \ _____  ___.__.|  | |__| ____ |  |___/  |_     
     /   |   \\____ \_/ __ \ /    \ |    |  \\__  \<   |  ||  | |  |/ ___\|  |  \   __\    
    /    |    \  |_> >  ___/|   |  \|    `   \/ __ \\___  ||  |_|  / /_/  >   Y  \  |      
    \_______  /   __/ \___  >___|  /_______  (____  / ____||____/__\___  /|___|  /__|      
            \/|__|        \/     \/        \/     \/\/            /_____/      \/          


Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.
Hit '<ctrl-d>' or type 'system:shutdown' or 'logout' to shutdown OpenDaylight.

opendaylight-user@root>

```

# Testing the API

There is a lot of software available that provides the ability to perform REST API calls. In this repository we include a Postman collection that features a set of example API calls. Simply download Postman from its official webpage (https://www.getpostman.com/) and import the collection included in _postman/northbound.json_.

# Arqueopterix SDN controller Northbound API

This repository contains the network controller developed for the project Arqueopterix (http://www.arqueopterix.es/en_US/). It contains a set of OSGi bundles developed for OpenDaylight Nitrogen SR3.

The repository contains two directories, each corresponding to one of the bundles that compose the complete system:

* **northbound-api:** Implements a REST API that allows third party applications to perform changes in the underlying network configuration in order to manage the QoS that applies to a specific network flow. This northbound API was developed in Swagger and can be accessed in the following link: https://app.swaggerhub.com/apis/cgiraldo/arqueopterix-policy_enforcement/0.1

* **openflowqos-service:** This bundle implements an abstraction layer over the openflowplugin bundle simplifying the way to add the necessary rules to the network devices so that they treat traffic in a different way based on the demanded QoS level. The resulting bundle exposes an interface in MD-SAL allowing other bundles to communicate with it and act over the network configuration.

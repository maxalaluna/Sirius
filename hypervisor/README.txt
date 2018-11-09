SIRIUS HYPERVISOR version 0.2

INSTRUCTIONS FOR INSTALLING AND RUNNING

For information, this code has been tested on Linux Ubuntu 14.04 LTS with JDK 7.0, Floodlight v1.2 and Eclipse IDE for Java Developers (version 4.5.2).

1) REQUIREMENTS

The Sirius hypervisor works as a regular Floodlight controller module. 
To install Floodlight controller: https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/overview
The main hypervisor's class is SiriusNetHypervisor.java (which belongs to the package net.floodlightcontroller.sirius.nethypervisor) and implements IFloodlightModule, INetHypervisorService, IOFMessageListener, IOFSwitchListener, ILinkDiscoveryListener, Observer.

2) REGISTER THE MODULE 

To load the Sirius hypervisor module on the startup it is necessary to add the the following line in the src/main/resources/META-INF/services/net.floodlightcontroller.core.module.IFloodlightModule:

 - net.floodlightcontroller.sirius.nethypervisor.SiriusNetHypervisor

And tell the module to be loaded, inserting the following line at the end of src/main/resources/floodlightdefault.properties:

 - net.floodlightcontroller.sirius.nethypervisor.SiriusNetHypervisor,
 
3) RUN SIRIUS

To run the Sirius hypervisor is simply to run Floodlight, executing the command in the terminal:

 - java -jar target/floodlight.jar
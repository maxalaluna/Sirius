SIRIUS ORCHESTRATOR version 0.2

INSTRUCTIONS FOR INSTALLING AND RUNNING

For information, this code has been tested on Windows with JDK 8.0, Tomcat 8.5 and Eclipse EE Web Developpers (version 4.6.3).

1) REQUIREMENTS

 - JDK 7 minimun +  HTTP/Servlet server (eg. Apache Tomcat) installed and configured

 - Set of LINUX Vms with public IPs, SSH server running on port TCP 22 and Docker installed and configured. The "installDocker" script is available in "sirius/orchestrator/script" to install Docker and all the required libraries.

 - The current version of the Orchestrator handles the automatic creation of the GRE tunnels between VMs belonging to the same cloud but the creation of the OpenVPN tunnels between gateway VMs is not supported yet. As a consequense, these latter tunnels have to be configured before running the Orchestrator. Note that the Orchestration can be run with a set of local VMs configured in a single cloud. In this case, no OpenVPN tunnel is required. 
 
2) VM PUBLIC/PRIVATE KEYS

SSH sessions on VMs are established based on public/private key authentication. We assume that each public or private cloud has a different key. All public and private keys must be placed in the "sirius/orchestrator/keys" folder. The key's name corresponds to the "key" tag attribute in the cloud section of the XML substrate file (see section 4 for further details). Key name suffixes are respectivelly ".pub" for public keys and "" for private keys. For instance, a remote cloud called "amazon" must have a private key file named "amazon" and a public key file named "amazon.pub". In order to grant the access to the remote VMs, public keys have to be added into the remote "ssh/authorized_key" files.  

3) TOPOLOGY FILES

Topology configurations are saved in XML files in the "sirius/orchestrator/xml" folder. The "console_file_prefix" property from the "sirius/orchestrator/console.properties" file defines the prefix name of the topology files to be used. This attribute allows to have different sets of topology files for test and production purpose. 

The substrate topology is the only one required, the substrate file will be "network0.xml" if the "console_file_prefix" is set to "network". This file contains the definition of the remote clouds (see section 4), the docker images (see section 5), the nodes (docker containers or OVS switches) and the links defined between the nodes.

The tenant topology files are saved from the Orchestrator, tenant ids 1, 2 or 3 will simply correspond to files "network1.xml", "network2.xml" and "network3.xml". Tenant networks can be removed from the Orchestrator interface or by deleting the corresponding XML file. 

4) CLOUD CREDENTIALS

Cloud credentials are configured in the cloud section of the substrate XML file as follows:

<cloud id="ID">
  <name>NAME</name>
  <provider>PROVIDER</provider>
  <username>USERNAME</username>
  <identity>IDENTITY</identity>
  <credential>CREDENTIAL</credential>
  <key>KEY</key>
</cloud>

The identity/credential couple corresponds to the username/password required to connect and use a remote cloud's VM. Such credentials can be obtained from online service like AWS STS for Amazon. The current version of the Orchestrator requires to have the same credentials for all VMs in the same cloud. If you'd like to have different credentials per cloud, a workaround is to configure various logical clouds in the substrate file, all pointing to the same remote cloud. 

The key attribute refers to the prefix name of the public and private key files used to connect the VMs. Usually, this attribute is the same as the cloud's name (for instance, "amazon.pub" may be used as public key file for VMs in Amazon). But sometimes, it could be helpful to have different clouds with the same key (for instance, various local clouds called "local1", "local2" and so deployed in the same platform for testing purpose, can use the same public key "local.pub").   

5) DOCKER IMAGES

Docker images are configured in the image section of the substrate XML file as follows:

<image>
  <name>host</name>
  <script>docker run -dit %NAME% --hostname %NAME% --net=none host bash</script>
</image>

During the synchronization file, the Orchestration will check that the required Docker images are installed on the remote VMs. If not, automatic upload and installation will be performed by the Orchestrator. Once the Docker image is installed, we have to detail the commands required to create and run a Docker container with a given image. 

These commands will be processed every time the Orchestrator needs to create a new container or reconfigure an running container. We can define as many commands as necessary using the "<script>" tag. The initial "docker run" command can be completed by more initialization commands required to start a remote server or configure a remote datebase for instance. The script commands are sequentially executed. Let's note that due to the Docker architecture, the initial "docker run" command could return immediately even if the container's creation is not fully completed.

In this example, we define a image called "host" and corresponding to a default Ubuntu image. The "%NAME%" variable can be used in the script command to replace the container's name. More variable names should be available in the next Orchestrator version.  

6) INSTALLATION

 - compile the JAVA code with the "ant" command and create the WAR file OR use the WAR file already provided with the files

 - move the WAR file to the "/opt/tomcat/webapps" folder (if Tomcat is the HTTP/Servlet server).

 - Set the environment variable "SUPERCLOUD_HOME" to "path/to/sirius/orchestrator"

 - Configure the configuration attributes in the "console.properties" file. The Orchestrator can work with default parameters but as least, the "console_admin_ip" (Orchestrator's IP address) and "console_file_prefix" (topology XML file name) attributes must be set.
 
7) RUNNING

We assume that Tomcat is installed as HTTP/Servlet on a Linux server and the Hypervisor is runnning. 

 - Go to "/opt/tomcat/bin" and run "./startup.sh" if Tomcat is installed on a Linux server.

 - Wait for the Orchestrator to synchronize the remote Cloud. According to the number and location of the remote VMs, this action may take a few minutes. Progress can be shown in the "catalina.out" file ("tail -f /opt/tomcat/logs/catalina.out"). 
 
 The synchronization with a public or private remote cloud can be disabled using the "public_cloud_sync" and "private_cloud_sync" attribute in the properties file. In this case, the Orchestration won't perform any verification as the current state of the remote containers or the GRE tunnels and we will assume that the substrate file is up-to-date. 

 In case the user wants to speed up the synchronization phase and maintain a minimum verification process, the "console_deep_sync" can be disabled in the properties file.
 
 - Wait for the Hypervisor to connect and synchronize the Orchestrator. The Hypervisor is configured as TCP client. If the connection is not made, check IP and TCP ports attributes in properties files. 

 - From a browser supporting HTML5, connect the Orchestor with "http:orchestrator-ip:8080/supercloud" and log in using the credentials defined in the properties file ("console_admin_username" and "console_admin_password").

 - The Tomcat server can be stopped through the "./shutdown.sh" command.

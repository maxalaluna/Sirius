#!/bin/bash
firstOctReq=10
minimumVersionLinux=3.10
minimumProcessorBits="x86_64"
ubuntu1404="14.04"
repo1404="deb https://apt.dockerproject.org/repo ubuntu-trusty main"
ubuntu1604="16.04"
repo1604="deb https://apt.dockerproject.org/repo ubuntu-xenial main"
dockerHello="Hello from Docker!"
dockerWorking="This message shows that your installation appears to be working correctly."
dockerPullStatusNew="Status: Downloaded newer image for ubuntu:latest"
dockerPullStatusUptodate="Status: Image is up to date for ubuntu:latest"
dockerCreateHost="Successfully built"
data=`date`
echo $data
#ifconfig=`ifconfig`
#echo $ifconfig

echo "Script responsible for install Docker in a Linux server."

versionLinux=`uname -r`
processorBits=`uname -i`
IFS='.' read -a versionLinuxArray <<< "$versionLinux"
versionLinuxFP=${versionLinuxArray[0]}"."${versionLinuxArray[1]}

echo "Installing bc"
sudo apt-get install -y bc >/dev/null 2>&1

st=`echo "$versionLinuxFP < $minimumVersionLinux" | bc -l`

if [ $st -eq 1 ] && [ "$processorBits" = "$minimumProcessorBits" ]; then
	echo "Docker requires a 64-bit installation regardless of your Ubuntu version and kernel must be 3.10 at minimum."
	exit
else

	dig=`dig ubuntu.com +short | grep .`
	#echo $dig
	sizeDig=${#dig}
	#echo $sizeDig

	if [ $sizeDig -eq 0 ]; then
        	echo "Script error!"
	        echo "DNS is not working. Please config it and re-run the script."
		exit
	else

	        ubuntuVersion=`lsb_release -r --short`

		if [ "$ubuntuVersion" = "$ubuntu1404" ]; then
			repo=$repo1404
		fi

		if [ "$ubuntuVersion" = "$ubuntu1604" ]; then
			repo=$repo1604
                fi

		echo "Updating the system..."
		sudo apt-get update >/dev/null 2>&1
		echo "Ensure that APT works with the https method, and that CA certificates are installed..."
		sudo apt-get install -y apt-transport-https ca-certificates >/dev/null 2>&1
		echo "Add the new GPG key."
		sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D >/dev/null 2>&1
		echo $repo | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null 2>&1
		sudo apt-get update >/dev/null 2>&1
		echo "Finished."
		echo "Installing Docker and recommended packages..."
		sudo apt-get install -y linux-image-extra-$(uname -r) linux-image-extra-virtual >/dev/null 2>&1
		sudo apt-get update -y >/dev/null 2>&1
		sudo apt-get install -y docker-engine >/dev/null 2>&1
		sudo service docker start  >/dev/null 2>&1
		testDockerHello=`sudo docker run hello-world | grep "$dockerHello"`
		testDockerWorking=`sudo docker run hello-world | grep "$dockerWorking"`

		if [ "$testDockerHello" = "$dockerHello" ] && [ "$testDockerWorking" = "$dockerWorking" ]; then

			echo "Finished."
			echo "Configuring docker user..."
			sudo groupadd docker >/dev/null 2>&1
			sudo usermod -aG docker $USER >/dev/null 2>&1
			`sed -i.bak 's/GRUB_CMDLINE_LINUX=/#GRUB_CMDLINE_LINUX=/g' /etc/default/grub`
			echo GRUB_CMDLINE_LINUX=\"cgroup_enable=memory swapaccount=1\" >> /etc/default/grub 
			sudo update-grub  >/dev/null 2>&1

			if [ "$ubuntuVersion" = "$ubuntu1604" ]; then

				sudo systemctl enable docker
			fi

			digServer=`dig ubuntu.com +short +identify`
			IFS=' ' read -a digServerArray <<< "$digServer"
			serverDNS=${digServerArray[3]}

			`sed -i.bak 's/DOCKER_OPTS=/#DOCKER_OPTS=/g' /etc/default/docker`
			echo "DOCKER_OPTS=\"--dns $serverDNS\"" >> /etc/default/docker
			sudo apt-get upgrade -y docker-engine >/dev/null 2>&1
			echo "Docker installed and configured with success!"
			echo "Pulling ubuntu container..."
			dockerPullUbuntu=`docker pull ubuntu | grep Status`

			if [ "$dockerPullUbuntu" = "$dockerPullStatusNew" ] || [ "$dockerPullUbuntu" = "$dockerPullStatusUptodate" ]; then

				echo "Finished."
				echo "Preparing container with name 'host'..."
				dataTimestamp=`date +"%s"`
				docker run ubuntu >/dev/null 2>&1
				mkdir -p ~/docker/mydockerbuild_$dataTimestamp
				echo "FROM ubuntu:latest" >> ~/docker/mydockerbuild_$dataTimestamp/Dockerfile
				echo "RUN apt-get -y update && apt-get install -y net-tools && apt-get install -y iputils-ping" >> ~/docker/mydockerbuild_$dataTimestamp/Dockerfile
				dockerCreateUbuntuHost=`docker build -t host ~/docker/mydockerbuild_$dataTimestamp/ | grep "$dockerCreateHost"`
				sizeDockerCreateUbuntuHost=${#dockerCreateUbuntuHost}

				if [ $sizeDockerCreateUbuntuHost -eq 0 ]; then

					echo "Error!"
                                        echo "Preparation container with name 'host' fail. Please re-run the script."
				else
					echo "Ubuntu container named 'host' created."
					echo "Installation complete."

				fi

			else
				echo "Error!"
				echo "Pull ubuntu image fail. Please re-run the script."
			fi
		else
			echo "Error!"
			echo "Docker NOT installed correctly. Please re-run the script."
			exit
		fi
 	fi
fi

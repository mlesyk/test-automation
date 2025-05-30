#!/bin/bash
# Ubuntu/Debian
wget https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.zip
unzip apache-jmeter-5.6.3.zip
sudo mv apache-jmeter-5.6.3 /usr/local/jmeter

export JMETER_HOME=/usr/local/jmeter
export PATH=$PATH:$JMETER_HOME/bin
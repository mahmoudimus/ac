#!/bin/sh

mkdir /home/vagrant/scripts
cd /home/vagrant/scripts

cp /vagrant/scripts/connect_scripts/* .
sudo ln -s /home/vagrant/scripts/atlas-run-connect.sh /usr/bin/atlas-run-connect
sudo echo '/home/vagrant/scripts/oracle-license.sh' >> ~/.profile

sudo npm install


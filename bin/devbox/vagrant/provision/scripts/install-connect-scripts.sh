#!/bin/sh

mkdir /home/vagrant/scripts
cd /home/vagrant/scripts

cp /vagrant/scripts/connect_scripts/* .
sudo ln -s /home/vagrant/scripts/atlas-run-connect.sh /usr/bin/atlas-run-connect

sudo npm install


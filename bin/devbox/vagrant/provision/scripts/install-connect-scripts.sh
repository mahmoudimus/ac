#!/bin/sh
echo 'Creating script directories'
mkdir /home/vagrant/scripts
mkdir /home/vagrant/scripts/cache
chmod a+w /home/vagrant/scripts/cache

echo 'Copying scripts'
sudo cp /vagrant/scripts/connect_scripts/* /home/vagrant/scripts/
sudo chmod a+x /home/vagrant/scripts/*.sh
echo 'Linking atlas-run-connect'
sudo ln -s /home/vagrant/scripts/atlas-run-connect.sh /usr/bin/atlas-run-connect
echo 'Changing .profile'
sudo echo '/home/vagrant/scripts/oracle-license.sh' >> /home/vagrant/.profile
echo 'Changing hostmane to localhost'
sudo sed -i "s/precise32/localhost/g" /etc/hosts
sudo sed -i "s/precise32/localhost/g" /etc/hostname
echo 'Running npm install'
cd /home/vagrant/scripts
npm install


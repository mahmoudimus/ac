#!/bin/sh
rm -r -f target
mkdir target
mkdir target/logs
echo "destroying existing box..."
vagrant destroy -f >> target/logs/vagrant-destroy.logs
echo "Done."
echo "Running provisioning scripts..."
vagrant up >> target/logs/vagrant-provision.logs
if [ "$(tail -1 target/logs/vagrant-provision.logs)" != "==> default: OK" ]; then echo "Failed. See logs."; exit 1;fi;
echo "Done."
echo "Packaging box..."
vagrant package --output target/atlassian-connect.box
echo "Done."
echo "Shipping box to S3..."
# For more details about the aws command, see https://extranet.atlassian.com/display/ARA/Vagrant+box
aws s3 cp target/atlassian-connect.box s3://atlassian-connect-devbox/atlassian-connect.box

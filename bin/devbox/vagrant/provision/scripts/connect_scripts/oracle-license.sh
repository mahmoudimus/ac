# Variable LS_COLORS is not set when Vagrant runs the provisioning steps
# It is set after running vagrant ssh
# better solution anyone?
if ! [ -z "$LS_COLORS" ]; then 
	cd /home/vagrant/scripts
	sudo node oracle-license.js
	rc=$?
	if [[ $rc != 0 ]] ; then
		sudo shutdown -h now "License terms not accepted. Shutting down."
	fi
fi
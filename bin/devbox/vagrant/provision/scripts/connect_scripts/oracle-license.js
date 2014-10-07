var sys = require("sys");
var fs = require('fs');
var licenseCheckFile='/home/vagrant/scripts/license.log';

fs.readFile(licenseCheckFile, function read(err, data) {
        if (err) {
			var exec = require('child_process').exec;
			console.log('│  Oracle Binary Code License Agreement for the Java SE Platform Products   │');
			console.log('│   and JavaFX                                                              │');
			console.log('│                                                                           │'); 
			console.log('│                                                                           │');  
			console.log('│ You MUST agree to the license available in http://java.com/license if     │');  
			console.log('│ you want to use Oracle JDK.                                               │'); 
			console.log('│                                                                           │'); 
			console.log('│ In order to install this package, you must accept the license terms, the  │');  
			console.log('│ "Oracle Binary Code License Agreement for the Java SE Platform Products   │');  
			console.log('│ and JavaFX ". Not accepting will cancel the installation.                 │');  
			console.log('│                                                                           │');
			console.log('│                                                                           │');
			console.log('│ Do you accept the Oracle Binary Code license terms?                       │');
			console.log('│ Type yes/no and press ENTER                                               │');
			var stdin = process.openStdin();
			stdin.addListener("data", function(d) {
			    var input = d.toString().trim();
	
				if(input == 'yes') {
					fs.writeFileSync(
						licenseCheckFile,
						'Oracle license accepted on ' + new Date().toString());
					welcome();
					process.exit(0);
				} else if(input == 'no') {
					process.exit(1);
				} else {
					logError();
				}
		
			});
			process.on('SIGINT', function() {
				//prevent escaping
				logError();
			});
        } else {
            welcome();
        }

        
    });

function welcome() {
    console.log("Welcome to your Atlassian Connect development box!");
    console.log("This is a development environment to build Atlassian Connect add-ons.");
    console.log("It is not supported in production.")
}
function logError() {
    console.log('Invalid input: do you accept the Oracle Binary Code license terms?');
    console.log('Type yes/no and press ENTER');
}

  

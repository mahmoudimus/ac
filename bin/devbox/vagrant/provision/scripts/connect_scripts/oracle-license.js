var sys = require("sys");
var fs = require('fs');

fs.readFile('/home/vagrant/scripts/license.log', function read(err, data) {
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
			console.log('│ (yes/no)                                                                  │');
			var stdin = process.openStdin();
			stdin.addListener("data", function(d) {
			    var input = d.toString().trim();
	
				if(input == 'yes') {
					fs.writeFileSync(
						'/home/vagrant/scripts/license.log', 
						'Oracle license accepted on ' + new Date().toString());
					console.log("Welcome to your Atlassian Connect development box!")
					process.exit(0);
				} else if(input == 'no') {
					process.exit(1);
				} else {
					console.log('Invalid input: yes/no');
				}
		
			});
			process.on('SIGINT', function() {
				//prevent escaping
				console.log('Invalid input: yes/no');
			});
        } 
        
    });


  

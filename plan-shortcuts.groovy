commonPlanConfiguration() {
    permissions() {
        loggedInUser(permissions: 'read')
    }
    notification(
            type: 'All Builds Completed',
            recipient: 'stash'
    )
    notification(
            type: 'Change of Build Status',
            recipient: 'watchers'
    )
}

productSnapshotPlanConfiguration(['productVersion']) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    variable(
            key: 'bamboo.product.version',
            value: '#productVersion'
    )
    trigger(
            type: 'cron',
            cronExpression: '0 30 20 ? * 1,2,3,4,5'
    )
    hipChatNotification()
}

pollingTrigger(['repositoryName']) {
    trigger(
            type: 'polling',
            strategy: 'periodically',
            frequency: '60'
    ) {
        repository(name: '#repositoryName')
    }
}

hipChatNotification() {
    notification(
            type: 'All Builds Completed',
            recipient: 'hipchat',
            apiKey: '${bamboo.atlassian.hipchat.apikey.password}',
            notify: 'false',
            room: 'Atlassian Connect Cloud Team'
    )
}

runTestsStage() {
    stage(
            name: 'Run Tests'
    ) {
        testJobsForConfluence(
                mavenProductParameters: ''
        )
        testJobsForJIRA(
                mavenProductParameters: ''
        )
        job(
                key: 'UTJ7',
                name: 'Unit Tests'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            mavenTestTask(
                    description: 'Run Unit Tests',
                    goal: 'clover2:setup package clover2:clover',
                    environmentVariables: ''
            )
            cloverReportArtifact(
                    name: 'Unit Tests'
            )
            cloverJSONArtifact(
                    name: 'Unit Tests'
            )
            cloverMiscConfiguration()
            cloverBambooTask()
        }
        job(
                key: 'QUNIT',
                name: 'QUnit Tests'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            mavenTestTask(
                    description: 'Run QUnit Tests using Karma',
                    goal: 'package -Pkarma-tests -DskipUnits',
                    environmentVariables: ''
            )
            artifactDefinition(
                    name: 'Karma Test Results',
                    location: 'tests/test-reports',
                    pattern: 'karma-results.xml',
                    shared: 'false'
            )
        }
        job(
                key:'JDOC',
                name:'Javadoc',
                description: 'Generates Javadoc'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            mavenTask(
                    description: 'Build Plugin and Generate Javadoc',
                    goal: 'install -DskipTests javadoc:javadoc',
            )
        }
        job(
                key: 'VALIDATE',
                name: 'Descriptor Validation',
                description: 'Validates that all public add-ons in the marketplace validate against the latest schema version'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            mavenTask(
                    description: 'Build Plugin',
                    goal: 'install -DskipTests',
            )
            task(
                    type: 'npm',
                    description: 'Install Node.js Dependencies for Marketplace Scripts',
                    command: 'install',
                    executable: 'Node.js 0.10',
                    workingSubDirectory: 'bin/marketplace'
            )
            task(
                    type: 'nodejs',
                    description: 'Generate Validator Test Output',
                    arguments: '--debug --type json --testReport=plugin/src/test/resources/descriptor/descriptor-validation-results.json',
                    script: 'bin/marketplace/validate-descriptors.js',
                    executable: 'Node.js 0.10'
            )
            setupVncTask()
            mavenTestTask(
                    description: 'Run Add-On Descriptor Validation Tests',
                    goal: '-pl plugin test -DdescriptorValidation=true -DskipTests',
                    environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome',
            )
            artifactDefinition(
                    name: 'Validator output',
                    location: 'plugin/src/test/resources/descriptor',
                    pattern: '*.json',
                    shared: 'false'
            )
        }
        job(
                key: 'DOCS',
                name: 'Developer Docs'
        ) {
            commonRequirements()
            requirement(
                    key: 'system.builder.node.Node.js 0.10',
                    condition: 'exists'
            )
            checkoutDefaultRepositoryTask()
            mavenTask(
                    description: 'Build Developer Documentation',
                    goal: 'install site -DskipTests',
            )
            artifactDefinition(
                    name: 'Documentation',
                    location: 'docs/target/gensrc/www',
                    pattern: '**/*.*',
                    shared: 'true'
            )
        }
    }
}

testJobsForConfluence(['mavenProductParameters']) {
    lifecycleTestJob(
            key: 'CLT',
            product: 'Confluence',
            testGroup: 'confluence',
            additionalMavenParameters: '#mavenProductParameters'
    )
    wiredTestJob(
            key: 'CWT',
            product: 'Confluence',
            testGroup: 'confluence',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCM',
            product: 'Confluence',
            testGroup: 'confluence-common-misc',
            groupName: 'Common Misc',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCI',
            product: 'Confluence',
            testGroup: 'confluence-common-iframe',
            groupName: 'Common iframe',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCL',
            product: 'Confluence',
            testGroup: 'confluence-common-lifecycle',
            groupName: 'Common Lifecycle',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITM',
            product: 'Confluence',
            testGroup: 'confluence-misc',
            groupName: 'Misc',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITI',
            product: 'Confluence',
            testGroup: 'confluence-iframe',
            groupName: 'iframe',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITT',
            product: 'Confluence',
            testGroup: 'confluence-item',
            groupName: 'Item',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITJ',
            product: 'Confluence',
            testGroup: 'confluence-jsapi',
            groupName: 'JS API FF',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITJC',
            product: 'Confluence',
            testGroup: 'confluence-jsapi',
            groupName: 'JS API Chrome',
            additionalMavenParameters: '#mavenProductParameters -Dwebdriver.browser=chrome'
    )
    integrationTestJob(
            key: 'CITA',
            product: 'Confluence',
            testGroup: 'confluence-macro',
            groupName: 'Macro',
            additionalMavenParameters: '#mavenProductParameters'
    )
}

testJobsForJIRA(['mavenProductParameters']) {
    lifecycleTestJob(
            key: 'JLT',
            product: 'JIRA',
            testGroup: 'jira',
            additionalMavenParameters: '#mavenProductParameters'
    )
    wiredTestJob(
            key: 'JWT',
            product: 'JIRA',
            testGroup: 'jira',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCM',
            product: 'JIRA',
            testGroup: 'jira-common-misc',
            groupName: 'Common Misc',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCI',
            product: 'JIRA',
            testGroup: 'jira-common-iframe',
            groupName: 'Common iframe',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCL',
            product: 'JIRA',
            testGroup: 'jira-common-lifecycle',
            groupName: 'Common Lifecycle',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITM',
            product: 'JIRA',
            testGroup: 'jira-misc',
            groupName: 'Misc',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITI',
            product: 'JIRA',
            testGroup: 'jira-iframe',
            groupName: 'iframe',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITT',
            product: 'JIRA',
            testGroup: 'jira-item',
            groupName: 'Item',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITJ',
            product: 'JIRA',
            testGroup: 'jira-jsapi',
            groupName: 'JS API FF',
            additionalMavenParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITJC',
            product: 'JIRA',
            testGroup: 'jira-jsapi',
            groupName: 'JS API Chrome',
            additionalMavenParameters: '#mavenProductParameters -Dwebdriver.browser=chrome'
    )
}

lifecycleTestJob(['key', 'product', 'testGroup', 'additionalMavenParameters']) {
    job(
            key: '#key',
            name: '#product - Lifecycle Tests'
    ) {
        artifactDefinition(
                name: 'Plugin JAR File',
                location: 'plugin/target',
                pattern: 'atlassian-connect-plugin.jar',
                shared: 'false'
        )
        commonRequirements()
        checkoutDefaultRepositoryTask()
        mavenInstallTask()
        mavenTestTask(
                description: 'Run Wired Lifecycle Tests for #product',
                goal: 'clover2:setup verify -pl plugin-lifecycle-tests -PpluginLifecycle,clover -DtestGroups=#testGroup -DskipUnits #additionalMavenParameters clover2:aggregate clover2:clover',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
        cloverReportArtifact(
                name: '#product - Lifecycle Tests'
        )
        cloverJSONArtifact(
                name: '#product - Lifecycle Tests'
        )
        cloverMiscConfiguration()
        cloverBambooTask()
    }
}

wiredTestJob(['key', 'product', 'testGroup', 'additionalMavenParameters']) {
    job(
            key: '#key',
            name: '#product - Wired Tests'
    ) {
        commonRequirements()
        checkoutDefaultRepositoryTask()
        mavenInstallTask()
        mavenTestTask(
                description: 'Run Wired Tests for #product',
                goal: 'clover2:setup verify -pl wired-tests -Pwired,clover -DtestGroups=#testGroup -DskipUnits #additionalMavenParameters clover2:aggregate clover2:clover',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
        cloverReportArtifact(
                name: '#product - Wired Tests'
        )
        cloverJSONArtifact(
                name: '#product - Wired Tests'
        )
        cloverMiscConfiguration()
        cloverBambooTask()
    }
}

integrationTestJob(['key', 'product', 'testGroup', 'groupName', 'additionalMavenParameters']) {
    job(
            key: '#key',
            name: '#product - IT #groupName'
    ) {
        commonRequirements()
        checkoutDefaultRepositoryTask()
        setupVncTask()
        mavenInstallTask()
        mavenTestTask(
                description: 'Run Integration Tests for #product #groupName',
                goal: 'verify -pl integration-tests -Pit -DtestGroups=#testGroup -DskipUnits #additionalMavenParameters',
                environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome',
        )
        defineWebDriverOutputArtefact()
    }
}

commonRequirements() {
    requirement(
            key: 'os',
            condition: 'equals',
            value: 'Linux'
    )
    requirement(
            key: 'system.builder.command.Bash',
            condition: 'exists'
    )
}

maven32Requirement() {
    requirement(
            key: 'system.builder.mvn3.Maven 3.2',
            condition: 'exists'
    )
}

checkoutDefaultRepositoryTask() {
    task(
            type: 'checkout',
            description: 'Checkout Default Repository',
            cleanCheckout: 'true'
    )
}

mavenInstallTask() {
    mavenTask(
            description: 'Install',
            goal: 'install -DskipUnits'
    )
}

mavenTask(['description', 'goal']) {
    mavenTaskImpl(
            description: '#description',
            goal: '#goal',
            environmentVariables: '',
            hasTests: 'false',
            testDirectory: ''
    )
}

mavenTestTask(['description', 'goal', 'environmentVariables']) {
    mavenTaskImpl(
            description: '#description',
            goal: '#goal',
            environmentVariables: '#environmentVariables',
            hasTests: 'true',

            // Runners for wired and integration tests use the non-standard report directory
            // target/group-[confluence|jira]/tomcat6x/surefire-reports
            testDirectory: '**/target/**/surefire-reports/*.xml'
    )
}

mavenTaskImpl(['description', 'goal', 'environmentVariables', 'hasTests', 'testDirectory']) {
    task(
            type: 'maven3',
            description: '#description',
            goal: '#goal -B -nsu -e',
            buildJdk: 'JDK 1.8',
            mavenExecutable: 'Maven 3.2',
            environmentVariables: '#environmentVariables',
            hasTests: '#hasTests',
            testDirectory: '#testDirectory'
    )
}

cloverReportArtifact(['name']) {
    artifactDefinition(
            name:'Clover Report (System) - #name',
            location:'plugin/target/site/clover',
            pattern:'**/*.*',
            shared:'true'
    )
}

cloverJSONArtifact(['name']) {
    artifactDefinition(
            name:'Coverage (JSON - System) - #name',
            pattern:'coverage-*.json',
            shared:'true'
    )
}

cloverMiscConfiguration() {
    miscellaneousConfiguration() {
        coverageJSON(
            enabled:'true'
        )
        clover(
            type:'custom',
            path:'plugin/target/site/clover'
        )
    }
}

cloverBambooTask() {
    task(
            type:'custom',
            createTaskKey:'com.atlassian.bamboo.plugins.bamboo-coverage-json-plugin:coverage-json-task',
            description:'',
            final:'true',
            format:'clover',
            location:'**/clover.xml'
    )
}

setupVncTask() {
    task(
            type: 'script',
            description: 'VNC Script Setup',
            scriptBody: '''

#!/bin/bash

function killVnc {
VNC_PID_FILE=`echo $HOME/.vnc/*:20.pid`
if [ -n "$VNC_PID_FILE" -a -f "$VNC_PID_FILE" ]; then
vncserver -kill :20 >/dev/null 2>&1
if [ -f  "$VNC_PID_FILE" ]; then
VNC_PID=`cat $VNC_PID_FILE`
echo "Killing VNC pid ($VNC_PID) directly..."
kill -9 $VNC_PID
vncserver -kill :20 >/dev/null 2>&1

if [ -f  "$VNC_PID_FILE" ]; then
echo "Failed to kill vnc server"
exit -1
fi
fi
fi
rm -f /tmp/.X11-unix/X20
rm -f /tmp/.X20-lock
}

displayEnv() {

echo "---------------------------------------------"
echo "Displaying Environment Variables"
	echo "---------------------------------------------"
	env
	echo "---------------------------------------------"
}

echo starting vncserver

killVnc

#echo vncserver :20
vncserver :20 >/dev/null 2>&1
echo vncserver started on :20

displayEnv

# Move the mouse pointer out of the way
# echo Moving mouse pointer to 10 10.
# xwarppointer abspos 10 10

'''
    )
}

defineWebDriverOutputArtefact() {
    artifactDefinition(
            name: 'HTML dumps and screenshots',
            location: 'integration-tests/target/webdriverTests',
            pattern: '**/*.*',
            shared: 'false'
    )
}

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

productSnapshotPlanConfiguration(['applicationVersion']) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    variable(
            key: 'bamboo.product.version',
            value: '#applicationVersion'
    )
    /* trigger(
            type: 'cron',
            cronExpression: '0 30 20 ? * 2,3,4,5,6'
    )
    hipChatNotification() */
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
            checkoutDefaultRepositoryTask()
            mavenTestTask(
                    description: 'Run Unit Tests',
                    goal: 'package -DskipDocs',
                    environmentVariables: ''
            )
        }
        job(
                key: 'QUNIT',
                name: 'QUnit Tests'
        ) {
            bashRequirement()
            checkoutDefaultRepositoryTask()
            mavenTestTask(
                    description: 'Run QUnit Tests using Karma',
                    goal: 'package -Pkarma-tests -DskipUnits -DskipDocs',
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
                key: 'VALIDATE',
                name: 'Descriptor Validation',
                description: 'Validates that all public add-ons in the marketplace validate against the latest schema version'
        ) {
            bashRequirement()
            checkoutDefaultRepositoryTask()
            mavenTask(
                    description: 'Build Developer Documentation',
                    goal: 'install -DskipDocs -DskipTests',
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
                    arguments: '--type json --testReport=plugin/src/test/resources/descriptor/descriptor-validation-results.json',
                    script: 'bin/marketplace/validate-descriptors.js',
                    executable: 'Node.js 0.10'
            )
            setupVncTask()
            mavenTestTask(
                    description: 'Run Add-On Descriptor Validation Tests',
                    goal: 'test -DdescriptorValidation=true -DskipDocs -DskipTests',
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
            requirement(
                    key: 'system.builder.node.Node.js 0.10',
                    condition: 'exists'
            )
            checkoutDefaultRepositoryTask()
            mavenTask(
                    description: 'Build Developer Documentation',
                    goal: 'install -DskipTests',
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
            mavenProductParameters: '#mavenProductParameters'
    )
    wiredTestJob(
            key: 'CWT',
            product: 'Confluence',
            testGroup: 'confluence',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCM',
            product: 'Confluence',
            testGroup: 'confluence-common-misc',
            groupName: 'Common Misc',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCI',
            product: 'Confluence',
            testGroup: 'confluence-common-iframe',
            groupName: 'Common iframe',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCJ',
            product: 'Confluence',
            testGroup: 'confluence-common-jsapi',
            groupName: 'Common JS API',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCL',
            product: 'Confluence',
            testGroup: 'confluence-common-lifecycle',
            groupName: 'Common Lifecycle',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITCR',
            product: 'Confluence',
            testGroup: 'confluence-common-rest',
            groupName: 'Common REST',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITM',
            product: 'Confluence',
            testGroup: 'confluence-misc',
            groupName: 'Misc',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITI',
            product: 'Confluence',
            testGroup: 'confluence-iframe',
            groupName: 'iframe',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITT',
            product: 'Confluence',
            testGroup: 'confluence-item',
            groupName: 'Item',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITJ',
            product: 'Confluence',
            testGroup: 'confluence-jsapi',
            groupName: 'JS API',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'CITA',
            product: 'Confluence',
            testGroup: 'confluence-macro',
            groupName: 'Macro',
            mavenProductParameters: '#mavenProductParameters'
    )
}

testJobsForJIRA(['mavenProductParameters']) {
    lifecycleTestJob(
            key: 'JLT',
            product: 'JIRA',
            testGroup: 'jira',
            mavenProductParameters: '#mavenProductParameters'
    )
    wiredTestJob(
            key: 'JWT',
            product: 'JIRA',
            testGroup: 'jira',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCM',
            product: 'JIRA',
            testGroup: 'jira-common-misc',
            groupName: 'Common Misc',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCI',
            product: 'JIRA',
            testGroup: 'jira-common-iframe',
            groupName: 'Common iframe',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCJ',
            product: 'JIRA',
            testGroup: 'jira-common-jsapi',
            groupName: 'Common JS API',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCL',
            product: 'JIRA',
            testGroup: 'jira-common-lifecycle',
            groupName: 'Common Lifecycle',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITCR',
            product: 'JIRA',
            testGroup: 'jira-common-rest',
            groupName: 'Common REST',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITM',
            product: 'JIRA',
            testGroup: 'jira-misc',
            groupName: 'Misc',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITI',
            product: 'JIRA',
            testGroup: 'jira-iframe',
            groupName: 'iframe',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITT',
            product: 'JIRA',
            testGroup: 'jira-item',
            groupName: 'Item',
            mavenProductParameters: '#mavenProductParameters'
    )
    integrationTestJob(
            key: 'JITJ',
            product: 'JIRA',
            testGroup: 'jira-jsapi',
            groupName: 'JS API',
            mavenProductParameters: '#mavenProductParameters'
    )
}

integrationTestJobForJIRA(['suffix', 'testGroup', 'mavenProductParameters']) {
    integrationTestJob(
            key: 'JIT#suffix',
            product: 'JIRA - ITs ',
            testGroup: '#testGroup',
            mavenProductParameters: '#mavenProductParameters'
    )
}

lifecycleTestJob(['key', 'product', 'testGroup', 'mavenProductParameters']) {
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
        checkoutDefaultRepositoryTask()
        mavenTestTask(
                description: 'Run Wired Lifecycle Tests for #product',
                goal: 'verify -PpluginLifecycle -DtestGroups=#testGroup -DskipUnits -DskipDocs #mavenProductParameters',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
    }
}

wiredTestJob(['key', 'product', 'testGroup', 'mavenProductParameters']) {
    job(
            key: '#key',
            name: '#product - Wired Tests'
    ) {
        checkoutDefaultRepositoryTask()
        mavenTestTask(
                description: 'Run Wired Tests for #product',
                goal: 'verify -Pwired -DtestGroups=#testGroup -DskipUnits -DskipDocs #mavenProductParameters',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
    }
}

integrationTestJob(['key', 'product', 'testGroup', 'groupName', 'mavenProductParameters']) {
    job(
            key: '#key',
            name: '#product - ITs #groupName'
    ) {
        bashRequirement()
        checkoutDefaultRepositoryTask()
        setupVncTask()
        mavenTestTask(
                description: 'Run Integration Tests for #product #groupName',
                goal: 'verify -Pit -DtestGroups=#testGroup -DskipUnits -DskipDocs #mavenProductParameters',
                environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome',
        )
        defineWebDriverOutputArtefact()
    }
}

bashRequirement() {
    requirement(
            key: 'system.builder.command.Bash',
            condition: 'exists'
    )
}

maven30Requirement() {
    requirement(
            key: 'system.builder.mvn3.Maven 3.0',
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
            mavenExecutable: 'Maven 3.0',
            environmentVariables: '#environmentVariables',
            hasTests: '#hasTests',
            testDirectory: '#testDirectory'
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

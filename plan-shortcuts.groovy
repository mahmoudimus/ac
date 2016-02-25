commonPlanConfiguration() {
    permissions() {
        loggedInUser(permissions: 'read')
    }
    notification(
            type: 'Change of Build Status',
            recipient: 'watchers'
    )
}

productSnapshotPlanConfiguration(['productVersion']) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    variable(
            key: 'product.version',
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
            type: 'Failed Builds and First Successful',
            recipient: 'hipchat',
            apiKey: '${bamboo.atlassian.hipchat.apikey.password}',
            notify: 'false',
            room: 'Atlassian Connect Cloud Team'
    )
}

stashNotification() {
    notification(
            type: 'All Builds Completed',
            recipient: 'stash'
    )
}

runTestsStage(['installMavenParameters', 'testMavenParameters']) {
    stage(
            name: 'Run Tests'
    ) {
        testJobsForConfluence(
                installMavenParameters: '#installMavenParameters',
                testMavenParameters: '#testMavenParameters'
        )
        testJobsForJIRA(
                installMavenParameters: '#installMavenParameters',
                testMavenParameters: '#testMavenParameters'
        )
        job(
                key: 'UTJ7',
                name: 'Unit Tests'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            mavenTestTask(
                    description: 'Run Unit Tests',
                    goal: 'clover2:setup package -Pclover clover2:aggregate clover2:clover #testMavenParameters',
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
            setupVncTask()
            mavenTestTask(
                    description: 'Run QUnit Tests using Karma',
                    goal: '-pl jsapi package -Pkarma-tests #testMavenParameters',
                    environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome'
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
                    goal: 'install -DskipTests javadoc:javadoc #installMavenParameters',
            )
        }
        job(
                key: 'VALIDATE',
                name: 'Descriptor Validation',
                description: 'Validates that all public add-ons in the marketplace validate against the latest schema version'
        ) {
            commonRequirements()
            checkoutDefaultRepositoryTask()
            setupVncTask()
            mavenTestTask(
                    description: 'Run Add-On Descriptor Validation Tests',
                    goal: '-pl tests/descriptor-validation-tests verify -PdescriptorValidation -DskipTests -am #testMavenParameters',
                    environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome',
            )
            artifactDefinition(
                    name: 'Global schema',
                    location: 'tests/descriptor-validation-tests/target/schema',
                    pattern: '*-global-schema.json',
                    shared: 'false'
            )
            artifactDefinition(
                    name: 'Validator output',
                    location: 'tests/descriptor-validation-tests/target',
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
                    goal: 'install site -DskipTests #installMavenParameters',
            )
            artifactDefinition(
                    name: 'Documentation',
                    location: 'docs/target/gensrc/www',
                    pattern: '**/*.*',
                    shared: 'true'
            )
            artifactDefinition(
                name: 'Plugin JAR File',
                location: 'plugin/target',
                pattern: 'atlassian-connect-plugin.jar',
                shared: 'false'
            )
        }
    }
}

testJobsForConfluence(['installMavenParameters', 'testMavenParameters']) {
    lifecycleTestJob(
            key: 'CLT',
            product: 'Confluence',
            testGroup: 'confluence',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    wiredTestJob(
            key: 'CWT',
            product: 'Confluence',
            testGroup: 'confluence',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'CITCM',
            product: 'Confluence',
            testGroup: 'confluence-common-misc',
            groupName: 'Common Misc',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'CITCI',
            product: 'Confluence',
            testGroup: 'confluence-common-iframe',
            groupName: 'Common iframe',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'CITCL',
            product: 'Confluence',
            testGroup: 'confluence-common-lifecycle',
            groupName: 'Common Lifecycle',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    confluenceIntegrationTestJob(
            key: 'CITM',
            testGroup: 'confluence-misc',
            groupName: 'Misc',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    confluenceIntegrationTestJob(
            key: 'CITI',
            testGroup: 'confluence-iframe',
            groupName: 'iframe',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    confluenceIntegrationTestJob(
            key: 'CITT',
            testGroup: 'confluence-item',
            groupName: 'Item',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    confluenceIntegrationTestJob(
            key: 'CITJF',
            testGroup: 'confluence-jsapi',
            groupName: 'JS API FF',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'CITJFC',
            product: 'Confluence',
            testGroup: 'confluence-common-jsapi',
            groupName: 'JS API Common FF',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    confluenceIntegrationTestJob(
            key: 'CITJC',
            testGroup: 'confluence-jsapi',
            groupName: 'JS API Chrome',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters -Dwebdriver.browser=chrome'
    )
    integrationTestJob(
            key: 'CITJCC',
            product: 'Confluence',
            testGroup: 'confluence-common-jsapi',
            groupName: 'JS API Common Chrome',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters -Dwebdriver.browser=chrome'
    )
    confluenceIntegrationTestJob(
            key: 'CITA',
            testGroup: 'confluence-macro',
            groupName: 'Macro',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
}

testJobsForJIRA(['installMavenParameters', 'testMavenParameters']) {
    lifecycleTestJob(
            key: 'JLT',
            product: 'JIRA',
            testGroup: 'jira',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    wiredTestJob(
            key: 'JWT',
            product: 'JIRA',
            testGroup: 'jira',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'JITCM',
            product: 'JIRA',
            testGroup: 'jira-common-misc',
            groupName: 'Common Misc',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'JITCI',
            product: 'JIRA',
            testGroup: 'jira-common-iframe',
            groupName: 'Common iframe',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'JITCL',
            product: 'JIRA',
            testGroup: 'jira-common-lifecycle',
            groupName: 'Common Lifecycle',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    jiraIntegrationTestJob(
            key: 'JITM',
            testGroup: 'jira-misc',
            groupName: 'Misc',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    jiraIntegrationTestJob(
            key: 'JITI',
            testGroup: 'jira-iframe',
            groupName: 'iframe',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    jiraIntegrationTestJob(
            key: 'JITT',
            testGroup: 'jira-item',
            groupName: 'Item',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    jiraIntegrationTestJob(
            key: 'JITJF',
            testGroup: 'jira-jsapi',
            groupName: 'JS API FF',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    integrationTestJob(
            key: 'JITJFC',
            testGroup: 'jira-common-jsapi',
            product: 'JIRA',
            groupName: 'JS API FF Common',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters'
    )
    jiraIntegrationTestJob(
            key: 'JITJC',
            testGroup: 'jira-jsapi',
            groupName: 'JS API Chrome',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters -Dwebdriver.browser=chrome'
    )
    integrationTestJob(
            key: 'JITJCC',
            testGroup: 'jira-common-jsapi',
            product: 'JIRA',
            groupName: 'JS API Common Chrome',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters -Dwebdriver.browser=chrome'
    )
}

lifecycleTestJob(['key', 'product', 'testGroup', 'installMavenParameters', 'testMavenParameters']) {
    job(
            key: '#key',
            name: '#product - Lifecycle Tests'
    ) {
        commonRequirements()
        checkoutDefaultRepositoryTask()
        mavenInstallTask(
                installMavenParameters: '#installMavenParameters'
        )
        mavenTestTask(
                description: 'Run Wired Lifecycle Tests for #product',
                goal: 'verify -pl tests/plugin-lifecycle-tests -PpluginLifecycle -DtestGroups=#testGroup -DskipUnits -DskipITs=false #testMavenParameters',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
        mavenTask(
                description: 'Generate Clover Report',
                goal: 'clover2:aggregate clover2:clover'
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

wiredTestJob(['key', 'product', 'testGroup', 'installMavenParameters', 'testMavenParameters']) {
    job(
            key: '#key',
            name: '#product - Wired Tests'
    ) {
        commonRequirements()
        checkoutDefaultRepositoryTask()
        mavenInstallTask(
                installMavenParameters: '#installMavenParameters'
        )
        mavenTestTask(
                description: 'Run Wired Tests for #product',
                goal: 'verify -pl tests/wired-tests -Pwired -DtestGroups=#testGroup -DskipITs=false -DskipUnits #testMavenParameters',
                environmentVariables: 'MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"',
        )
        mavenTask(
                description: 'Generate Clover Report',
                goal: 'clover2:aggregate clover2:clover'
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

integrationTestJob(['key', 'product', 'testGroup', 'groupName', 'installMavenParameters', 'testMavenParameters']) {
    projectIntegrationTestJob(
        key: '#key',
        product: '#product',
        testGroup: '#testGroup',
        groupName: '#groupName',
        installMavenParameters: '#installMavenParameters',
        testMavenParameters: '#testMavenParameters',
        project: 'tests/core-integration-tests'
    )
}

jiraIntegrationTestJob(['key', 'testGroup', 'groupName', 'installMavenParameters', 'testMavenParameters']) {
    projectIntegrationTestJob(
            key: '#key',
            product: 'JIRA',
            testGroup: '#testGroup',
            groupName: '#groupName',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters',
            project: 'jira/jira-integration-tests'
    )
}

confluenceIntegrationTestJob(['key', 'testGroup', 'groupName', 'installMavenParameters', 'testMavenParameters']) {
    projectIntegrationTestJob(
            key: '#key',
            product: 'Confluence',
            testGroup: '#testGroup',
            groupName: '#groupName',
            installMavenParameters: '#installMavenParameters',
            testMavenParameters: '#testMavenParameters',
            project: 'confluence/confluence-integration-tests'
    )
}

projectIntegrationTestJob(['key', 'product', 'testGroup', 'groupName', 'installMavenParameters', 'testMavenParameters', 'project']) {
    job(
            key: '#key',
            name: '#product - IT #groupName'
    ) {
        commonRequirements()
        checkoutDefaultRepositoryTask()
        setupVncTask()
        mavenInstallTask(
                installMavenParameters: '#installMavenParameters'
        )
        mavenTestTask(
                description: 'Run Integration Tests for #product #groupName',
                goal: 'verify -pl #project -Pit -DtestGroups=#testGroup -DskipUnits -DskipITs=false #testMavenParameters',
                environmentVariables: 'DISPLAY=":20" MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m" CHROME_BIN=/usr/bin/google-chrome',
        )
        defineWebDriverOutputArtefact(
                project: '#project'
        )
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
            key: 'system.builder.mvn3.Maven 3.3',
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

mavenInstallTask(['installMavenParameters']) {
    mavenTask(
            description: 'Install',
            goal: 'clover2:setup install -Pclover -DskipUnits #installMavenParameters'
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
            goal: '#goal -B -e',
            buildJdk: 'JDK 1.8',
            mavenExecutable: 'Maven 3.3',
            environmentVariables: '#environmentVariables',
            hasTests: '#hasTests',
            testDirectory: '#testDirectory'
    )
}

cloverReportArtifact(['name']) {
    artifactDefinition(
            name:'Clover Report (System) - #name',
            location:'target/site/clover',
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
            path:'target/site/clover'
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
            location:'target/site/clover/clover.xml'
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

defineWebDriverOutputArtefact(['project']) {
    artifactDefinition(
            name: 'HTML dumps and screenshots',
            location: '#project/target/webdriverTests',
            pattern: '**/*.*',
            shared: 'false'
    )
}

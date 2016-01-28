plan(
        projectKey: 'CONNECT',
        key: 'ACD',
        name: 'Cloud Plugin - develop',
        description: 'Tests atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    pollingTrigger(repositoryName: 'Atlassian Connect (develop)')
    stashNotification()
    hipChatNotification()
    notification(
            type: 'Failed Builds and First Successful',
            recipient: 'committers'
    )
    runTestsStage(
            installMavenParameters: '',
            testMavenParameters: ''
    )
    stage(
            name: 'Start Release',
            manual: 'true'
    ) {
        job(
                key: 'REL',
                name: 'Merge and increment version'
        ) {
            maven32Requirement()
            checkoutDefaultRepositoryTask()
            task(
                    type: 'script',
                    description: 'Merge to master and increment POM versions on develop',
                    script: 'bin/advance_versions_and_tag.sh'
            )
        }
    }
}

plan(
        projectKey: 'CONNECT',
        key: 'CF',
        name: 'Cloud Plugin - Feature branches',
        description: 'Tests feature branches of atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (branch builds)')
    pollingTrigger(repositoryName: 'Atlassian Connect (branch builds)')
    stashNotification()
    notification(
            type: 'All Builds Completed',
            recipient: 'committers'
    )
    branchMonitoring(
            enabled: 'true',
            matchingPattern: '(feature|issue)/.*',
            timeOfInactivityInDays: '14',
            notificationStrategy: 'INHERIT',
            remoteJiraBranchLinkingEnabled: 'true'
    )
    variable(
            key: 'maven.parameters',
            value: ''
    )
    variable(
            key: 'maven.test.parameters',
            value: ''
    )

    runTestsStage(
            installMavenParameters: '${bamboo.maven.parameters}',
            testMavenParameters: '${bamboo.maven.parameters} ${bamboo.maven.test.parameters}'
    )
}

plan(
        projectKey: 'CONNECT',
        key: 'CCM',
        name: 'Cloud Plugin - SNAPSHOT CONF',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest Confluence SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            productVersion: '6.0.0-SNAPSHOT'
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForConfluence(
                installMavenParameters: '',
                testMavenParameters: '-Datlassian.confluence.version=${bamboo.product.version}'
        )
    }
}

plan(
        projectKey: 'CONNECT',
        key: 'CJM',
        name: 'Cloud Plugin - SNAPSHOT JIRA',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest JIRA SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            productVersion: '7.2.0-SNAPSHOT'
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                installMavenParameters: '',
                testMavenParameters: '-Datlassian.jira.version=${bamboo.product.version}'
        )
    }
}

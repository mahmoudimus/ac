plan(
        projectKey: 'CONNECT',
        key: 'ACDX',
        name: 'Cloud Plugin - develop X',
        description: 'Tests atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    /* pollingTrigger(repositoryName: 'Atlassian Connect (develop)')
    hipChatNotification() */
    runTestsStage()
    stage(
            name: 'Start Release',
            manual: 'true'
    ) {
        job(
                key: 'VTM',
                name: 'Version, tag and merge',
                description: 'Sets new pom versions, git-tags, merges to master, pushes to master and develop'
        ) {
            maven30Requirement()
            checkoutDefaultRepositoryTask()
            task(
                    type: 'script',
                    description: 'Merge to Master and Update Development Version',
                    script: 'advance_versions_and_tag.sh',
                    workingSubDirectory: 'bin'
            )
        }
    }
}

plan(
        projectKey: 'CONNECT',
        key: 'CFX',
        name: 'Cloud Plugin - Feature branches X',
        description: 'Tests feature branches of atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (branch builds)')
    /* pollingTrigger(repositoryName: 'Atlassian Connect (branch builds)')
    notification(
            type: 'All Builds Completed',
            recipient: 'committers'
    ) */
    branchMonitoring(
            enabled: 'true',
            matchingPattern: '(feature|issue)/.*',
            timeOfInactivityInDays: '14',
            notificationStrategy: 'INHERIT',
            remoteJiraBranchLinkingEnabled: 'true'
    )

    runTestsStage()
}

plan(
        projectKey: 'CONNECT',
        key: 'CCMX',
        name: 'Cloud Plugin - SNAPSHOT CONF X',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest Confluence SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            applicationVersion: '5.8-SNAPSHOT',
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForConfluence(
                mavenProductParameters: '-Datlassian.confluence.version=${bamboo_product_version}'
        )
    }
}

plan(
        projectKey: 'CONNECT',
        key: 'CJMX',
        name: 'Cloud Plugin - SNAPSHOT JIRA X',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest JIRA SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            applicationVersion: '6.5-SNAPSHOT',
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo_product_version}'
        )
    }
}

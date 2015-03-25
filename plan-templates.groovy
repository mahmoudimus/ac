plan(
        projectKey: 'CONNECT',
        key: 'ACD',
        name: 'Cloud Plugin - develop',
        description: 'Tests atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    pollingTrigger(repositoryName: 'Atlassian Connect (develop)')
    hipChatNotification()
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
        key: 'CF',
        name: 'Cloud Plugin - Feature branches',
        description: 'Tests feature branches of atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (branch builds)')
    pollingTrigger(repositoryName: 'Atlassian Connect (branch builds)')
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

    runTestsStage()
}

productSnapshotPlan(
        prefix: 'C',
        shortName: 'CONF',
        product: 'Confluence',
        testGroup: 'confluence',
        productVersion: '5.8-SNAPSHOT',
        mavenProductParameters: '-Datlassian.confluence.version=${bamboo_product_version}'
)

productSnapshotPlan(
        prefix: 'J',
        shortName: 'JIRA',
        product: 'JIRA',
        testGroup: 'jira',
        productVersion: '6.5-SNAPSHOT',
        mavenProductParameters: '-Datlassian.jira.version=${bamboo_product_version}'
)

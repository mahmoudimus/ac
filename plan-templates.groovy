plan(
        projectKey: 'CONNECT',
        key: 'CPDMPT',
        name: 'Cloud Plugin - develop (Templatized)',
        description: 'Tests atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop)')
    pollingTrigger()
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
        key: 'CFPT',
        name: 'Cloud Plugin - Feature branches (Templatized)',
        description: 'Tests feature branches of atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (branch builds)')
    pollingTrigger()
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
        mavenProductParameters: '-Datlassian.confluence.version=5.8-SNAPSHOT'
)

productSnapshotPlan(
        prefix: 'J',
        shortName: 'JIRA',
        product: 'JIRA',
        testGroup: 'jira',
        mavenProductParameters: '-Datlassian.jira.version=6.5-SNAPSHOT'
)

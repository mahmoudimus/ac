plan(
        projectKey: 'CONNECT',
        key: 'ACD1998',
        name: 'zzzACDEV-1998 Cloud Plugin - develop',
        description: 'Tests atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (develop) ACDEV-1998')
    pollingTrigger(repositoryName: 'Atlassian Connect (develop) ACDEV-1998')
    runTestsStage()
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
        key: 'CF1998',
        name: 'zzzACDEV-1998 Cloud Plugin - Feature branches',
        description: 'Tests feature branches of atlassian-connect-plugin'
) {
    commonPlanConfiguration()
    repository(name: 'Atlassian Connect (branch builds) ACDEV-1998')
    pollingTrigger(repositoryName: 'Atlassian Connect (branch builds) ACDEV-1998')
    branchMonitoring(
            enabled: 'true',
            matchingPattern: '(feature|issue)/.*',
            timeOfInactivityInDays: '14',
            remoteJiraBranchLinkingEnabled: 'true'
    )

    runTestsStage()
}

plan(
        projectKey: 'CONNECT',
        key: 'CCM1998',
        name: 'zzzACDEV-1998 Cloud Plugin - SNAPSHOT CONF',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest Confluence SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            productVersion: '5.9.1-SNAPSHOT',
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
        key: 'CJM1998',
        name: 'zzzACDEV-1998 Cloud Plugin - SNAPSHOT JIRA',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest JIRA SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            productVersion: '7.0.0-DA-SNAPSHOT',
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo_product_version}'
        )
    }
}

plan(
        projectKey: 'CONNECT',
        key: 'CJMR1998',
        name: 'zzzACDEV-1998 Cloud Plugin - SNAPSHOT JIRA - Renaissance',
        description: 'Tests the develop branch of atlassian-connect-plugin against the latest JIRA SNAPSHOT version'
) {
    productSnapshotPlanConfiguration(
            productVersion: '7.0.0-DA-SNAPSHOT',
    )
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo_product_version} -Djvmargs="-Datlassian.darkfeature.com.atlassian.jira.config.CoreFeatures.LICENSE_ROLES_ENABLED=true"'
        )
    }
}

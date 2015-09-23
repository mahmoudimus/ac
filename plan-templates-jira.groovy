plan(
        projectKey: 'PLUGINS',
        key: 'ARA71',
        name: 'Atlassian Connect Plugin Master JIRA 7.1',
        description: 'Tests the master branch of atlassian-connect against the latest JIRA SNAPSHOT version 7.1.0'
) {
    commonPlanConfiguration()
    repository(name: 'atlassian-connect')
    repository(name: 'JIRA master stash full history')
    trigger(
            type: 'cron',
            cronExpression: '0 30 20 ? * 1,2,3,4,5'
    )
    hipChatNotification()
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo.plugin.builds.jira.trunk.version}'
        )
    }
}

plan(
        projectKey: 'PLUGINS',
        key: 'ARA70DA',
        name: 'Atlassian Connect Plugin Master JIRA 7.0 DA',
        description: 'Tests the master branch of atlassian-connect against the latest JIRA SNAPSHOT version 7.0 DA'
) {
    commonPlanConfiguration()
    repository(name: 'atlassian-connect')
    repository(name: 'JIRA master stash full history')
    trigger(
            type: 'cron',
            cronExpression: '0 30 20 ? * 1,2,3,4,5'
    )
    hipChatNotification()
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo.plugin.builds.jira.7.0.da.version}'
        )
    }
}


plan(
        projectKey: 'PLUGINS',
        key: 'ARA70',
        name: 'Atlassian Connect Plugin Master JIRA 7.0',
        description: 'Tests the master branch of atlassian-connect against the latest JIRA SNAPSHOT version 7.0'
) {
    commonPlanConfiguration()
    repository(name: 'atlassian-connect')
    repository(name: 'JIRA master stash full history')
    trigger(
            type: 'cron',
            cronExpression: '0 30 20 ? * 1,2,3,4,5'
    )
    hipChatNotification()
    stage(
            name: 'Run Tests'
    ) {
        testJobsForJIRA(
                mavenProductParameters: '-Datlassian.jira.version=${bamboo.plugin.builds.jira.7.0.stable.version}'
        )
    }
}
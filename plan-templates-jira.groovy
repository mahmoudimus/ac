plan(
        projectKey: 'PLUGINS',
        key: 'ARA',
        name: 'Atlassian Connect (Remote Apps) Plugin Master',
        description: 'Tests the master branch of JIRA against the latest atlassian-connect-plugin version'
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
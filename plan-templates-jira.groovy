plan(
        projectKey: 'PLUGINS',
        key: 'AC',
        name: 'Atlassian Connect Plugin',
        description: 'Tests the master branch of atlassian-connect against the JIRA trunk version'
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

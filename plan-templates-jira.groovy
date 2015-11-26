plan(
        projectKey: 'PLUGINS',
        key: 'AC',
        name: 'Atlassian Connect Plugin',
        description: 'Tests the master branch of atlassian-connect against the JIRA trunk version'
) {
    commonPlanConfiguration()
    repository(name: 'atlassian-connect (master)')
    repository(name: 'JIRA master stash full history')
    pollingTrigger(repositoryName: 'JIRA master stash full history')
    notification(
            type: 'Failed Builds and First Successful',
            recipient: 'committers'
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

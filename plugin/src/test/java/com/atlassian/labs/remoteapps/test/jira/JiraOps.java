package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.Tester;
import org.apache.commons.lang.RandomStringUtils;

public class JiraOps
{
    public static long createProject(TestedProduct<? extends Tester> product, String projectKey, String projectName)
    {
        return product.visit(ViewProjectsPage.class)
                .openCreateProjectDialog()
                .setKey(projectKey)
                .setName(projectName)
                .submitSuccess()
                .getProjectId();
    }

    public static long createProject(TestedProduct<? extends Tester> product)
    {
        String projectKey = RandomStringUtils.randomAlphabetic(4);
        String projectName = "Project " + projectKey;

        return createProject(product, projectKey, projectName);
    }
}

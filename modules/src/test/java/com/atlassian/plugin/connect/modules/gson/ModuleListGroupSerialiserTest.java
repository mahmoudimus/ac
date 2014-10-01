package com.atlassian.plugin.connect.modules.gson;

import com.atlassian.plugin.connect.jira.module.JiraConnectModuleList;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModuleListGroupSerialiserTest
{
    @Test
    public void foo()
    {
        ModuleListGroup moduleListGroup = new ModuleListGroup();
        JiraConnectModuleList jiraConnectModuleList = JiraConnectModuleList.newJiraModuleListBean().withJiraEntityProperties(
                EntityPropertyModuleBean.newEntityPropertyModuleBean().withKey("myKey").build()
        ).build();

        moduleListGroup.getModuleLists().put(JiraConnectModuleList.class, jiraConnectModuleList);

        Gson gson = new GsonBuilder().registerTypeAdapter(ModuleListGroup.class, new ModuleListGroupSerialiser()).create();
        String json = gson.toJson(moduleListGroup);
        System.out.println(json);
    }

}
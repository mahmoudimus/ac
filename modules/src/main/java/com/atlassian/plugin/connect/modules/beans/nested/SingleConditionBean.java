package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BeanWithParams;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.google.common.base.Objects;

/**
 * Conditions can be added to modules to display them only when all the given conditions are true.
 *
 * Single Conditions can take optional parameters.
 * These parameters will be passed in to the condition's init() method as a map of string key/value pairs before any condition checks are performed.
 *
 * To invert a condition, add the attribute ``invert="true"`` to the condition element.
 * This is useful where you want to show the section if a certain condition is not satisfied.
 *
 * Single Conditions must contain a *condition* attribute with the name of the condition to check.
 *
 * The valid condition names are as follows:
 *
 *#### CONFLUENCE
 * {@see com.atlassian.plugin.connect.modules.beans.ConfluenceConditions#CONDITION_LIST}
 *<br>
 *<br>
 *### JIRA
 * {@see com.atlassian.plugin.connect.modules.beans.JiraConditions#CONDITION_LIST}
 *
 * 
 *#### JIRA condition parameter mappings
 * The following table shows the condition parameters available for `has_issue_permission` and `has_project_permission` in Atlassian Connect module declarations and how they map to the permissions described in the [Permissions](https://docs.atlassian.com/jira/latest/com/atlassian/jira/security/Permissions.html) class documentation.
 * <br><br>
 * <table summary="JIRA condition parameter mappings">
 * <thead>
 * <tr><th>JIRA condition parameters</th><th>Atlassian Connect equivalent</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>ADMINISTER</td><td>admin</td></tr>
 * <tr><td>USE</td><td>use</td></tr>
 * <tr><td>SYSTEM\_ADMIN</td><td>sysadmin</td></tr>
 * <tr><td>PROJECT\_ADMIN</td><td>project</td></tr>
 * <tr><td>BROWSE</td><td>browse</td></tr>
 * <tr><td>CREATE\_ISSUE</td><td>create</td></tr>
 * <tr><td>EDIT\_ISSUE</td><td>edit</td></tr>
 * <tr><td>EDIT\_ISSUE</td><td>update</td></tr>
 * <tr><td>SCHEDULE\_ISSUE</td><td>scheduleissue</td></tr>
 * <tr><td>ASSIGNABLE\_USER</td><td>assignable</td></tr>
 * <tr><td>ASSIGN\_ISSUE</td><td>assign</td></tr>
 * <tr><td>RESOLVE\_ISSUE</td><td>resolv</td></tr>
 * <tr><td>CLOSE\_ISSUE</td><td>close</td></tr>
 * <tr><td>WORKLOG\_EDIT\_ALL</td><td>worklogeditall</td></tr>
 * <tr><td>WORKLOG\_EDIT\_OWN</td><td>worklogeditown</td></tr>
 * <tr><td>WORKLOG\_DELETE\_OWN</td><td>worklogdeleteown</td></tr>
 * <tr><td>WORKLOG\_DELETE\_ALL</td><td>worklogdeleteall</td></tr>
 * <tr><td>WORK\_ISSUE</td><td>work</td></tr>
 * <tr><td>LINK\_ISSUE</td><td>link</td></tr>
 * <tr><td>DELETE\_ISSUE</td><td>delete</td></tr>
 * <tr><td>CREATE\_SHARED\_OBJECTS</td><td>sharefilters</td></tr>
 * <tr><td>MANAGE\_GROUP\_FILTER\_SUBSCRIPTIONS</td><td>groupsubscriptions</td></tr>
 * <tr><td>MOVE\_ISSUE</td><td>move</td></tr>
 * <tr><td>SET\_ISSUE\_SECURITY</td><td>setsecurity</td></tr>
 * <tr><td>USER\_PICKER</td><td>pickusers</td></tr>
 * <tr><td>VIEW\_VERSION\_CONTROL</td><td>viewversioncontrol</td></tr>
 * <tr><td>MODIFY\_REPORTER</td><td>modifyreporter</td></tr>
 * <tr><td>VIEW\_VOTERS\_AND\_WATCHERS</td><td>viewvotersandwatchers</td></tr>
 * <tr><td>MANAGE\_WATCHER\_LIST</td><td>managewatcherlist</td></tr>
 * <tr><td>BULK\_CHANGE</td><td>bulkchange</td></tr>
 * <tr><td>COMMENT\_EDIT\_ALL</td><td>commenteditall</td></tr>
 * <tr><td>COMMENT\_EDIT\_OWN</td><td>commenteditown</td></tr>
 * <tr><td>COMMENT\_DELETE\_OWN</td><td>commentdeleteown</td></tr>
 * <tr><td>COMMENT\_DELETE\_ALL</td><td>commentdeleteall</td></tr>
 * <tr><td>ATTACHMENT\_DELETE\_OWN</td><td>attachdeleteown</td></tr>
 * <tr><td>ATTACHMENT\_DELETE\_ALL</td><td>attachdeleteall</td></tr>
 * <tr><td>CREATE\_ATTACHMENT</td><td>attach</td></tr>
 * <tr><td>COMMENT\_ISSUE</td><td>comment</td></tr>
 * <tr><td>VIEW\_WORKFLOW\_READONLY</td><td>viewworkflowreadonly</td></tr>
 * </tbody>
 * </table>
 *<br>
 *<br>
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#SINGLE_CONDITION_EXAMPLE}
 * @schemaTitle Single Condition
 * @since 1.0
 */
@SchemaDefinition("singleCondition")
public class SingleConditionBean extends BeanWithParams implements ConditionalBean
{
    @Required
    private String condition;

    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean invert;

    public SingleConditionBean()
    {
        this.condition = "";
        this.invert = false;
    }

    public SingleConditionBean(SingleConditionBeanBuilder builder)
    {
        super(builder);

        if (null == condition)
        {
            this.condition = "";
        }
        if (null == invert)
        {
            this.invert = false;
        }
    }

    public String getCondition()
    {
        return condition;
    }

    public Boolean isInvert()
    {
        return invert;
    }

    public static SingleConditionBeanBuilder newSingleConditionBean()
    {
        return new SingleConditionBeanBuilder();
    }

    public static SingleConditionBeanBuilder newSingleConditionBean(SingleConditionBean defaultBean)
    {
        return new SingleConditionBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(condition, invert);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof SingleConditionBean))
        {
            return false;
        }
        else
        {
            final SingleConditionBean that = (SingleConditionBean) obj;
            return Objects.equal(condition, that.condition) &&
                    Objects.equal(invert, that.invert);
        }
    }
}

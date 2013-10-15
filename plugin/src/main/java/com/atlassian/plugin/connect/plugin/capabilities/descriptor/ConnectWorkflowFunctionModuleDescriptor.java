package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.dom4j.Element;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ConnectWorkflowFunctionModuleDescriptor extends WorkflowFunctionModuleDescriptor {

    public ConnectWorkflowFunctionModuleDescriptor(JiraAuthenticationContext authenticationContext,
                                                   OSWorkflowConfigurator workflowConfigurator,
                                                   ComponentClassManager componentClassManager,
                                                   ModuleFactory moduleFactory) {
        super(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Override
    protected String getParameterName() {
        return super.getParameterName(); // TODO delete this override
    }

    @Override
    public String getHtml(String resourceName, AbstractDescriptor descriptor) {
        return getHtml(resourceName);
    }

    @Override
    public boolean isOrderable() {
        return true; // all remote post-functions are orderable
    }

    @Override
    public boolean isUnique() {
        return super.isUnique(); // TODO
    }

    @Override
    public boolean isDeletable() {
        return true; // all remote post-functions are deletable
    }

    @Override
    public boolean isAddable(String actionType) {
        return true; // remote post-functions can be added to any transition
    }

    @Override
    public Integer getWeight() {
        return null; // JIRA assumes only system post-functions should have weight
    }

    @Override
    public boolean isDefault() {
        return false; // remote post-functions are not added by default
    }

    @Override
    protected String getParamValue(Element element, String paramName) {
        return super.getParamValue(element, paramName); // TODO delete this override
    }

    @Override
    public Class<WorkflowPluginFunctionFactory> getImplementationClass() {
        return super.getImplementationClass(); // TODO
    }

    @Override
    public int compareTo(AbstractWorkflowModuleDescriptor<WorkflowPluginFunctionFactory> o) {
        return super.compareTo(o); // TODO delete this override
    }

    @Override
    public boolean isEditable() {
        return true; // TODO only if edit view is specified
    }

    @Override
    protected void assertResourceExists(String type, String name) throws PluginParseException {
        // no-op
    }

    @Override
    public String getHtml(String resourceName) {
        if (JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW.equals(resourceName)) {
            // render 'view'
            return "remote view"; // TODO
        } else if (JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS.equals(resourceName)) {
            // render 'create'
            return "remote create"; // TODO
        } else if (JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS.equals(resourceName)) {
            // render 'edit'
            return "remote edit"; // TODO
        }
        throw new IllegalArgumentException("Unknown resourceName " + resourceName);
    }

    @Override
    public String getHtml(String resourceName, Map<String, ?> startingParams) {
        return getHtml(resourceName);
    }

    @Override
    public void writeHtml(String resourceName, Map<String, ?> startingParams, Writer writer) throws IOException {
        writer.write(getHtml(resourceName));
    }

    @Override
    public WorkflowPluginFunctionFactory getModule() {
        return super.getModule(); // let JIRA do the lazy creation itself TODO is this cool?
    }

    @Override
    protected WorkflowPluginFunctionFactory createModule() {
        throw new UnsupportedOperationException("NIY"); // TODO
    }

    @Override
    public String getDescription() {
        return super.getDescription(); // TODO
    }

    @Override
    public String getName() {
        return super.getName(); // TODO
    }

    @Override
    public String getText(String key) {
        return super.getText(key); // TODO delete this override
    }

    @Override
    public String getText(String key, Object params) {
        return super.getText(key, params); // TODO delete this override
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern) {
        // no-op
    }

    @Override
    protected void loadClass(Plugin plugin, Element element) throws PluginParseException {
        // no-op
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException {
        // no-op
    }

    @Override
    public void enabled() {
        // no-op
    }

    @Override
    public void disabled() {
        // no-op
    }

    @Override
    public void destroy() {
        // TODO
    }

    @Override
    public void destroy(Plugin plugin) {
        super.destroy(plugin);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEnabledByDefault() {
        return super.isEnabledByDefault();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSystemModule() {
        return super.isSystemModule();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSingleton() {
        return super.isSingleton();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected boolean isSingletonByDefault() {
        return super.isSingletonByDefault();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getCompleteKey() {
        return super.getCompleteKey();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getPluginKey() {
        return super.getPluginKey();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getKey() {
        return super.getKey();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Class<WorkflowPluginFunctionFactory> getModuleClass() {
        return super.getModuleClass();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getParams() {
        return super.getParams();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getI18nNameKey() {
        return super.getI18nNameKey();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getDescriptionKey() {
        return super.getDescriptionKey();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors() {
        return super.getResourceDescriptors();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(String type) {
        return super.getResourceDescriptors(type);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public ResourceLocation getResourceLocation(String type, String name) {
        return super.getResourceLocation(type, name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(String type, String name) {
        return super.getResourceDescriptor(type, name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Float getMinJavaVersion() {
        return super.getMinJavaVersion();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean satisfiesMinJavaVersion() {
        return super.satisfiesMinJavaVersion();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void setPlugin(Plugin plugin) {
        super.setPlugin(plugin);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Plugin getPlugin() {
        return super.getPlugin();    //To change body of overridden methods use File | Settings | File Templates.
    }
}

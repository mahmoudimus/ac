package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.plugin.module.PluginModuleFactory;
import com.atlassian.confluence.plugin.module.PluginModuleHolder;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.apache.commons.io.IOUtils;
import org.dom4j.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.atlassian.confluence.core.BodyType.XHTML;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created by mjensen on 7/05/14.
 */
public class ConnectContentTemplateModuleDescriptor extends ContentTemplateModuleDescriptor {
    private final I18NBeanFactory i18NBeanFactory;
    private final LocaleManager localeManager;
    private PluginModuleHolder<PageTemplate> realPluginModuleHolder;

    public ConnectContentTemplateModuleDescriptor(ModuleFactory moduleFactory, I18NBeanFactory i18NBeanFactory, LocaleManager localeManager) {
        super(moduleFactory, i18NBeanFactory, localeManager);
        this.i18NBeanFactory = i18NBeanFactory;
        this.localeManager = localeManager;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull final Element element) throws PluginParseException {
        super.init(plugin, element);
        realPluginModuleHolder = PluginModuleHolder.getInstance(new PluginModuleFactory<PageTemplate>() {
            @Override
            public PageTemplate createModule() {
                PageTemplate result = new PageTemplate();
                result.setBodyType(XHTML);
                result.setContent(getTemplateContent(getTemplateLocator(element)));
                result.setModuleCompleteKey(new ModuleCompleteKey(getCompleteKey()));

                I18NBean i18nBean = i18NBeanFactory.getI18NBean(localeManager.getSiteDefaultLocale());
                result.setName(i18nBean.getText(isBlank(getI18nNameKey()) ? "create.content.plugin.plugin.default-template-name" : getI18nNameKey()));
                if (isNotBlank(getDescriptionKey()))
                    result.setDescription(i18nBean.getText(getDescriptionKey()));
                return result;
            }
        });
    }

    @Override
    public void enabled() {
        realPluginModuleHolder.enabled(getModuleClass());
    }

    @Override
    public void disabled() {
        realPluginModuleHolder.disabled();
    }

    @Override
    public PageTemplate getModule() {
        return realPluginModuleHolder.getModule();
    }

    private String getTemplateContent(URL templateLocator) {
        InputStream inputStream = null;
        try {
            inputStream = templateLocator.openStream();
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving template data from URL: " + templateLocator);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private URL getTemplateLocator(Element element) {
        final Resources resources = Resources.fromXml(element);

        ResourceLocation templateLocation = resources.getResourceLocation("download", "template");
        if (templateLocation == null) {
            throw new PluginParseException("You must specify a template resource for the <content-template> tag. Add <resource name=\"template\" type=\"download\" location=\"<insert-path-to-your-template>/template.xml\"/> as a child element of <content-template>.");
        }

        final URL templateLocator = getPlugin().getResource(templateLocation.getLocation());
        if (templateLocator == null) {
            throw new PluginParseException("Could not load template XML at: " + templateLocation.getLocation());
        }
        return templateLocator;
    }
}

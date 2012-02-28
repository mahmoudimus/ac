package com.atlassian.labs.remoteapps.integration.streams;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.ActivityObjectTypes;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.ActivityVerbs;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsException;
import com.atlassian.streams.api.StreamsFeed;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.spi.StreamsActivityProvider;
import com.atlassian.streams.spi.UserProfileAccessor;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

import static com.atlassian.streams.api.ActivityVerbs.newVerbFactory;

/**
 *
 */
public class RemoteAppsActivityProvider implements StreamsActivityProvider
{
    private static final Logger log = LoggerFactory.getLogger(RemoteAppsActivityProvider.class);

    private final BundleContext bundleContext;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final UserProfileAccessor userProfileAccessor;
    private final WebResourceManager webResourceManager;
    private final I18nResolver i18nResolver;
    
    public RemoteAppsActivityProvider(BundleContext bundleContext, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties, UserProfileAccessor userProfileAccessor, WebResourceManager webResourceManager, I18nResolver i18nResolver)
    {
        this.bundleContext = bundleContext;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.userProfileAccessor = userProfileAccessor;
        this.webResourceManager = webResourceManager;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public StreamsFeed getActivityFeed(ActivityRequest activityRequest) throws StreamsException
    {
        Map<Long, StreamsEntry> entries = Maps.newTreeMap();
        for (Bundle bundle : bundleContext.getBundles())
        {
            String header = (String) bundle.getHeaders().get("Remote-App");
            if (header != null)
            {
                Map<String,String> props = OsgiHeaderUtil.parseHeader(header).get("installer");
                StreamsEntry entry = toStreamsEntry(props, pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle)));
                if (entry != null)
                {
                    entries.put(bundle.getLastModified(), entry);
                }
            }
        }
        StreamsFeed feed = new StreamsFeed("Remote Apps", entries.values(), Option.<String>none());
        return feed;
    }

    private StreamsEntry toStreamsEntry(Map<String,String> props, final Plugin plugin)
    {
        final URI fakeUri = URI.create(applicationProperties.getBaseUrl());

        StreamsEntry.ActivityObject activityObject = new StreamsEntry.ActivityObject(StreamsEntry.ActivityObject.params()
                                                                     .id("").alternateLinkUri(URI.create(""))
                                                                     .activityObjectType(ActivityObjectTypes.status()));
 
        final UserProfile userProfile = userProfileAccessor.getUserProfile(props.get("user"));

        StreamsEntry.Renderer renderer = new StreamsEntry.Renderer()
        {
            public StreamsEntry.Html renderTitleAsHtml(StreamsEntry entry)
            {
                String userHtml = (userProfile.getProfilePageUri().isDefined()) ? "<a href=\""+userProfile.getProfilePageUri().get()+"\"  class=\"activity-item-user activity-item-author\">" + userProfile.getUsername() + "</a>" : userProfile.getUsername();
                return new StreamsEntry.Html(userHtml + " installed Remote App '" + plugin.getName() + "'");
            }

            public Option<StreamsEntry.Html> renderSummaryAsHtml(StreamsEntry entry)
            {
                return Option.none();
            }

            public Option<StreamsEntry.Html> renderContentAsHtml(StreamsEntry entry)
            {
                return Option.none();
            }
        };

        URI iconUri = null;
        for (ModuleDescriptor descriptor : plugin.getModuleDescriptors())
        {
            if (ApplicationType.class.isAssignableFrom(descriptor.getModuleClass()))
            {
                iconUri = ((ApplicationType)descriptor.getModule()).getIconUrl();
            }
        }
        if (iconUri == null)
        {
            log.warn("Missing icon for plugin " + plugin.getKey());
            return null;
        }
        ActivityVerb verb = newVerbFactory(ActivityVerbs.STANDARD_IRI_BASE).newVerb("add");

        StreamsEntry streamsEntry = new StreamsEntry(StreamsEntry.params()
                .id(fakeUri)
                .postedDate(new DateTime(Long.parseLong(props.get("date"))))
                .authors(ImmutableNonEmptyList.of(userProfile))
                .addActivityObject(activityObject)
                .verb(verb)
                .addLink(iconUri, StreamsActivityProvider.ICON_LINK_REL)
                .alternateLinkUri(fakeUri)
                .renderer(renderer)
                .applicationType(applicationProperties.getDisplayName()), i18nResolver);
        return streamsEntry;
    }
}

package it.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.rest.client.RemoteCQLSearchService;
import com.atlassian.confluence.rest.client.RemoteCQLSearchServiceImpl;
import com.atlassian.confluence.rest.client.RemoteContentPropertyService;
import com.atlassian.confluence.rest.client.RemoteContentPropertyServiceImpl;
import com.atlassian.confluence.rest.client.RemoteContentService;
import com.atlassian.confluence.rest.client.RemoteContentServiceImpl;
import com.atlassian.confluence.rest.client.RemoteLongTaskService;
import com.atlassian.confluence.rest.client.RemoteSpaceService;
import com.atlassian.confluence.rest.client.RemoteSpaceServiceImpl;
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider;
import com.atlassian.confluence.rest.client.impl.RemoteLongTaskServiceImpl;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;

import java.util.concurrent.Executors;

public class ConfluenceRestClient
{
    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

    private RemoteContentService contentService;
    private RemoteSpaceService spaceService;
    private RemoteContentPropertyService contentPropertyService;
    private RemoteLongTaskService longTaskService;
    private RemoteCQLSearchService cqlSearchService;

    public ConfluenceRestClient(ConfluenceTestedProduct product)
    {
        AuthenticatedWebResourceProvider authenticatedWebResourceProvider = new AuthenticatedWebResourceProvider(
                ConfluenceRestClientFactory.newClient(),product.getProductInstance().getBaseUrl(), "");
        TestUser user = ConnectTestUserFactory.admin(product);
        authenticatedWebResourceProvider.setAuthContext(
                user.getUsername(), user.getPassword().toCharArray());
        contentService = new RemoteContentServiceImpl(authenticatedWebResourceProvider, executor);
        spaceService = new RemoteSpaceServiceImpl(authenticatedWebResourceProvider, executor);
        contentPropertyService = new RemoteContentPropertyServiceImpl(authenticatedWebResourceProvider, executor);
        cqlSearchService = new RemoteCQLSearchServiceImpl(authenticatedWebResourceProvider, executor);
        longTaskService = new RemoteLongTaskServiceImpl(authenticatedWebResourceProvider, executor);
    }

    public RemoteSpaceService spaces()
    {
        return spaceService;
    }

    public RemoteContentService content()
    {
        return contentService;
    }

    public RemoteContentPropertyService contentProperties()
    {
        return contentPropertyService;
    }

    public RemoteLongTaskService longTasks()
    {
        return longTaskService;
    }

    public RemoteCQLSearchService cqlSearch()
    {
        return cqlSearchService;
    }
}

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
import com.atlassian.confluence.rest.client.RestClientFactory;
import com.atlassian.confluence.rest.client.authentication.AuthenticatedWebResourceProvider;
import com.atlassian.confluence.rest.client.impl.RemoteLongTaskServiceImpl;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.confluence.util.ConfluenceTestUserFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

public class ConfluenceRestClient {
    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

    private RemoteContentService contentService;
    private RemoteSpaceService spaceService;
    private RemoteContentPropertyService contentPropertyService;
    private RemoteLongTaskService longTaskService;
    private RemoteCQLSearchService cqlSearchService;

    public ConfluenceRestClient(ConfluenceTestedProduct product) {
        this(product, new ConfluenceTestUserFactory(product).admin());
    }

    public ConfluenceRestClient(ConfluenceTestedProduct product, TestUser admin) {
        this(product, admin.getUsername(), admin.getPassword());

    }

    public ConfluenceRestClient(ConfluenceTestedProduct product, String username, String password) {
        AuthenticatedWebResourceProvider authenticatedWebResourceProvider = new AuthenticatedWebResourceProvider(
                RestClientFactory.newClient(), product.getProductInstance().getBaseUrl(), "");
        authenticatedWebResourceProvider.setAuthContext(username, password.toCharArray());
        contentService = new RemoteContentServiceImpl(authenticatedWebResourceProvider, executor);
        spaceService = new RemoteSpaceServiceImpl(authenticatedWebResourceProvider, executor);
        contentPropertyService = new RemoteContentPropertyServiceImpl(authenticatedWebResourceProvider, executor);
        cqlSearchService = new RemoteCQLSearchServiceImpl(authenticatedWebResourceProvider, executor);
        longTaskService = new RemoteLongTaskServiceImpl(authenticatedWebResourceProvider, executor);
    }

    public RemoteSpaceService spaces() {
        return spaceService;
    }

    public RemoteContentService content() {
        return contentService;
    }

    public RemoteContentPropertyService contentProperties() {
        return contentPropertyService;
    }

    public RemoteLongTaskService longTasks() {
        return longTaskService;
    }

    public RemoteCQLSearchService cqlSearch() {
        return cqlSearchService;
    }
}

package com.atlassian.plugin.remotable.container.service.license;

import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.license.RemotablePluginLicense;
import com.atlassian.plugin.remotable.api.service.license.RemotablePluginLicenseRetriever;
import com.atlassian.plugin.remotable.host.common.service.license.LicenseDetailsRepresentation;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import javax.annotation.Nullable;

/**
 */
public class ContainerRemotablePluginLicenseRetriever implements RemotablePluginLicenseRetriever
{
    private final HostHttpClient hostHttpClient;

    public ContainerRemotablePluginLicenseRetriever(HostHttpClient hostHttpClient)
    {
        this.hostHttpClient = hostHttpClient;
    }

    @Override
    public Promise<RemotablePluginLicense> retrieve()
    {
        return hostHttpClient.newRequest(URI.create("/rest/remotable-plugins/latest/license"))
                .get()
                .<RemotablePluginLicense>transform()
                    .ok(new Function<Response, RemotablePluginLicense>()
                    {
                        @Override
                        public RemotablePluginLicense apply(final Response input)
                        {
                            ObjectMapper objectMapper = new ObjectMapper();
                            try
                            {
                                LicenseDetailsRepresentation details = objectMapper.readValue(input.getEntityStream(), LicenseDetailsRepresentation.class);
                                return details;
                            }
                            catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .notFound(new Function<Response, RemotablePluginLicense>()
                    {
                        @Override
                        public RemotablePluginLicense apply(@Nullable final Response input)
                        {
                            return null;
                        }
                    })
                    .otherwise(new Function<Throwable, RemotablePluginLicense>()
                    {
                        @Override
                        public RemotablePluginLicense apply(@Nullable final Throwable input)
                        {
                            return null;
                        }
                    })
                    .toPromise();
    }
}

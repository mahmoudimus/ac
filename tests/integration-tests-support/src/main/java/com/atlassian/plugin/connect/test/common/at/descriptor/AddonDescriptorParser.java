package com.atlassian.plugin.connect.test.common.at.descriptor;

import cc.plural.jsonij.JPath;
import cc.plural.jsonij.parser.ParserException;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AddonDescriptorParser {

    private String addonUrl;

    public AddonDescriptorParser(String addonUrl) {
        this.addonUrl = addonUrl;
    }

    public String getAddonKey() {
        return getDescriptorValue("key");
    }

    private String getDescriptorValue(String key) {
        String value;
        try {
            value = JPath.evaluate(descriptor.get(), key).getString();
        } catch (ParserException | IOException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    private ResettableLazyReference<String> descriptor = new ResettableLazyReference<String>() {
        @Override
        protected String create() throws Exception {
            try {
                String addonKey = transformResponse(
                        new HttpGet(addonUrl),
                        new HashSet<>(singletonList(200)),
                        "Error while downloading descriptor from " + addonUrl,
                        response -> {
                            try {
                                return EntityUtils.toString(response.getEntity());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        false
                );

                if (isBlank(addonKey)) {
                    throw new IllegalStateException("Could not find key in downloaded descriptor");
                }

                return addonKey;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private <T> T transformResponse(HttpUriRequest request, Set<Integer> acceptableCodes,
                                    String errorMessage, Function<CloseableHttpResponse, T> transformer, boolean forceJson)
            throws IOException {
        CloseableHttpResponse response = null;
        T result = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            if (forceJson) {
                request.addHeader("Content-Type", "application/json");
            }
            request.addHeader("Cache-Control", "no-cache");
            response = client.execute(request);
            if (!acceptableCodes.isEmpty() && !acceptableCodes.contains(response.getStatusLine().getStatusCode())) {
                throw new RuntimeException(
                        errorMessage + response.toString());
            }
            if (transformer != null) {
                result = transformer.apply(response);
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return result;
    }
}

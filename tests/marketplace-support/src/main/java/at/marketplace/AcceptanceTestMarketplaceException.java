package at.marketplace;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AcceptanceTestMarketplaceException extends RuntimeException {
    public AcceptanceTestMarketplaceException(String message, CloseableHttpResponse response)
            throws IOException {
        super(message + ": " + "\n" + response.getStatusLine() + entity(response));
    }

    private static String entity(CloseableHttpResponse response)
            throws IOException {
        if (response.getEntity() != null) {
            return "\n" + EntityUtils.toString(response.getEntity());
        } else {
            return "";
        }
    }
}

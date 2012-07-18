package com.atlassian.labs.remoteapps.container.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Registers apps with an Atlassian host
 */
public class AppRegister
{
    private static final Logger log = LoggerFactory.getLogger(AppRegister.class);

    public static void registerApp(URI host, String appKey, String localMountBaseUrl) throws IOException
    {
        URL url = new URL(host.toString() + "/rest/remoteapps/latest/installer");
        OutputStream out = null;
        try{
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            String authorizationString = "Basic " + DatatypeConverter.printBase64Binary(
                    "admin:admin".getBytes(Charset.defaultCharset()));
            uc.setRequestProperty ("Authorization", authorizationString);
            uc.setRequestMethod("POST");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            out = uc.getOutputStream();
            out.write(
                    ("url=" + URLEncoder.encode(localMountBaseUrl, "UTF-8") +
                            "&token=").getBytes(Charset.defaultCharset()));
            out.close();
            int response = uc.getResponseCode();
            log.debug("Registration response '" + response + "': " + IOUtils.toString(
                    uc.getInputStream()));
            uc.getInputStream().close();
            log.info("Registered '" + appKey + "' at " + host);
            // todo: handle errors
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
}

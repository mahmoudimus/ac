package com.atlassian.plugin.remotable.container.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
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
        URL url = new URL(host.toString() + "/rest/remotable-plugins/latest/installer");
        OutputStream out = null;
        try
        {
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
            int status = uc.getResponseCode();
            InputStream is;
            if (status >= 400)
            {
                is = uc.getErrorStream();
                log.error("Registration of '" + appKey + "' at '" + host + "' failed:\n" + IOUtils.toString(is));
            }
            else
            {
                is = uc.getInputStream();
                log.info("Registered '" + appKey + "' at '" + host + "'");
            }
            is.close();
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
}

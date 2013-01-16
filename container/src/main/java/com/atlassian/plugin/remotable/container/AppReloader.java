package com.atlassian.plugin.remotable.container;

import com.atlassian.plugin.remotable.descriptor.DescriptorAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.atlassian.plugin.remotable.container.util.AppRegister.registerApp;

/**
 * Reregisters the app descriptor with the host app if it detects a change
 */
public class AppReloader
{
    private final DescriptorAccessor descriptorAccessor;
    private final String localMountBaseUrl;
    private final Set<URI> foundHosts;
    private final Timer timer;
    private static final Logger log = LoggerFactory.getLogger(AppReloader.class);


    public AppReloader(DescriptorAccessor descriptorAccessor, String localMountBaseUrl,
            Set<URI> foundHosts)
    {
        this.descriptorAccessor = descriptorAccessor;
        this.localMountBaseUrl = localMountBaseUrl;
        this.foundHosts = foundHosts;
        this.timer = new Timer("Container app reloader", false);

        this.timer.scheduleAtFixedRate(new ScanTask(), 0, 1000);
    }

    public void shutdown()
    {
        timer.cancel();
    }

    private class ScanTask extends TimerTask
    {
        private long lastModified = 0;
        private final String appKey;
        private final File descriptor;


        public ScanTask()
        {
            try
            {
                this.descriptor = new File(descriptorAccessor.getDescriptorUrl().toURI());
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException("shouldn't happen", e);
            }
            this.appKey = descriptorAccessor.getKey();
        }
        @Override
        public void run()
        {
            if (descriptor.lastModified() != lastModified)
            {
                log.info(
                        "Detected a change in the descriptor " + this.descriptor.getPath() + ". Reregistering. . .");
                for (URI host : foundHosts)
                {
                    try
                    {
                        registerApp(host, appKey, localMountBaseUrl);
                    }
                    catch (IOException e)
                    {
                        log.error("Unable to register app: " + e.getMessage(), e);
                    }
                }

                lastModified = descriptor.lastModified();
            }
        }
    }
}

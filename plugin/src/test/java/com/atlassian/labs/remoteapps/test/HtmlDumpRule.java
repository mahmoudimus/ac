package com.atlassian.labs.remoteapps.test;

import com.atlassian.webdriver.AtlassianWebDriver;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 17/01/12
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlDumpRule extends TestWatchman
{
    private static final Logger log = LoggerFactory.getLogger(HtmlDumpRule.class);

    private final AtlassianWebDriver driver;
    private String destinationFolder;

    public HtmlDumpRule(AtlassianWebDriver driver)
    {
        this.driver = driver;
    }


    @Override
        public void starting(final FrameworkMethod method)
        {
            log.info("----- Starting " + method.getName());

            destinationFolder = "target/webdriverTests/" + method.getMethod().getDeclaringClass().getName();

            File dir = new File(destinationFolder);
            // Clean up the directory for the next run
            if (dir.exists()) {
                dir.delete();
            }

            dir.mkdirs();
        }

        @Override
        public void succeeded(final FrameworkMethod method)
        {
            log.info("----- Succeeded " + method.getName());
        }

        @Override
        public void failed(final Throwable e, final FrameworkMethod method)
        {
            String baseFileName =  destinationFolder + "/" + method.getName();
            File dumpFile = new File(baseFileName + ".html");
            log.error(e.getMessage(), e);
            log.info("----- Test Failed. " + e.getMessage());

            log.info("----- At page: " + driver.getCurrentUrl());
            log.info("----- Dumping page source to: " + dumpFile.getAbsolutePath());

            // Take a screen shot and dump it.
            driver.dumpSourceTo(dumpFile);
            driver.takeScreenshotTo(new File(baseFileName + ".png"));

        }

        @Override
        public void finished(final FrameworkMethod method)
        {
            log.info("----- Finished " + method.getName());
        }
}

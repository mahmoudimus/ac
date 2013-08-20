package com.atlassian.plugin.connect.test.junit;

import java.io.File;

import com.atlassian.webdriver.AtlassianWebDriver;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

// using the deprecated code here because we have older version of JUnit on our classpath we don't really control :(
public final class HtmlDumpRule extends TestWatchman
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtlassianWebDriver driver;

    private int htmlDumpCount = 0;
    private int screenShotCount = 0;

    private String destinationFolder;
    private String dumpFileName;

    public HtmlDumpRule(AtlassianWebDriver driver)
    {
        this.driver = checkNotNull(driver);
    }

    @Override
    public void starting(FrameworkMethod fm)
    {
        logger.info("----- Starting {}", getMethodName(fm));

        destinationFolder = "target/webdriverTests/" + getClassName(fm);
        dumpFileName = getMethodName(fm);

        cleanDirectory(destinationFolder);
    }

    @Override
    public void succeeded(FrameworkMethod fm)
    {
        logger.info("----- Succeeded {}", getMethodName(fm));
    }

    @Override
    public void failed(Throwable e, FrameworkMethod fm)
    {
        logger.error(e.getMessage(), e);
        logger.info("----- Test Failed: {}", e.getMessage());
        logger.info("----- At page: " + driver.getCurrentUrl());

        dumpHtml();
        takeScreenShot();
    }

    @Override
    public void finished(FrameworkMethod fm)
    {
        logger.info("----- Finished {}", getMethodName(fm));
    }

    public void dumpHtml()
    {
        final File dumpFile = getDumpFile("_" + htmlDumpCount++ + ".html");
        logger.info("----- Dumping HTML page source to: '{}'", dumpFile.getAbsolutePath());
        driver.dumpSourceTo(dumpFile);
    }

    public void takeScreenShot()
    {
        final File dumpFile = getDumpFile("_" + screenShotCount++ + ".png");
        logger.info("----- Taking screen shot to: '{}'", dumpFile.getAbsolutePath());
        driver.takeScreenshotTo(dumpFile);
    }

    private File getDumpFile(String suffix)
    {
        checkNotNull(destinationFolder, "Destination folder can't be null, did the test actually start?");
        checkNotNull(dumpFileName, "File name can't be null, did the test actually start?");

        return new File(destinationFolder, dumpFileName + suffix);
    }

    private static void cleanDirectory(String path)
    {
        final File dir = new File(path);
        if (dir.exists())
        {
            dir.delete();
        }

        dir.mkdirs();
    }

    private static String getMethodName(FrameworkMethod method)
    {
        return method.getName();
    }

    private static String getClassName(FrameworkMethod method)
    {
        return method.getMethod().getDeclaringClass().getName();
    }
}
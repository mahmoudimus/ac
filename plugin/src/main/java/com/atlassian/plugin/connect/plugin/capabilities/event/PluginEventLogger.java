package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.inject.Named;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.event.events.PluginEnabledEvent;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Named
public class PluginEventLogger
{
    private final Charset utf8;
    public static final String EOL = System.getProperty("line.separator");
    
    private File logFile;

    public PluginEventLogger()
    {
        this.utf8 = Charset.forName("UTF-8");
    }
    
    private void ensureLogFile() throws IOException
    {
        if(null == logFile)
        {
            this.logFile = createLogFile();
            Files.touch(logFile);
        }
        else if(!logFile.exists())
        {
            Files.touch(logFile);
        }
    }

    private File createLogFile()
    {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tmpDir,"connect-plugin-events.log");
    }

    public void log(Plugin plugin, String eventName) throws IOException
    {
        ensureLogFile();
        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getName()).append(" ! ").append(eventName).append(EOL);
        appendToLog(sb.toString());
    }

    public void log(ModuleDescriptor module, String eventName) throws IOException
    {
        ensureLogFile();
        StringBuilder sb = new StringBuilder();
        sb.append(module.getPlugin().getName()).append(":").append(module.getKey()).append(" ! ").append(eventName).append(EOL);
        appendToLog(sb.toString());
    }
    
    private void appendToLog(String message) throws IOException
    {
        Files.append(message, logFile, utf8);
    }
}

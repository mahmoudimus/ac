package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

public interface Module
{
    void update(Element el);

    Iterable<Pair<String, HttpServlet>> getResources();
}

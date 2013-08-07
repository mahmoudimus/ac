package com.atlassian.plugin.connect.test.server.module;

import javax.servlet.http.HttpServlet;

import com.atlassian.fugue.Pair;

import org.dom4j.Element;

public interface Module
{
    void update(Element el);

    Iterable<Pair<String, HttpServlet>> getResources();
}

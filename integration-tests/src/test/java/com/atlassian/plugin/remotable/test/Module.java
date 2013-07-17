package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

interface Module
{
    void update(Element el);

    Option<Pair<String, HttpServlet>> getResource();
}

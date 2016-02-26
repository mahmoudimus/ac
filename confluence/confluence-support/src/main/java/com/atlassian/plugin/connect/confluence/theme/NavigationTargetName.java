package com.atlassian.plugin.connect.confluence.theme;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The enum values should match the navigation targets specified in
 * <a href="https://developer.atlassian.com/static/connect/docs/latest/javascript/Navigator-target.html">the connect developer docs</a>.
 */
public enum NavigationTargetName {
    dashboard,
    spaceview,
    contentview;
    private static final Logger log = LoggerFactory.getLogger(NavigationTargetName.class);
    private static final Map<NavigationTargetName, List<NavigationTargetOverrideInfo>> navTargetNameMap;

    static {
        navTargetNameMap = Maps.newEnumMap(NavigationTargetName.class);
        for (NavigationTargetOverrideInfo navTarget : NavigationTargetOverrideInfo.values()) {
            if (!navTargetNameMap.containsKey(navTarget.getNavigationTargetName())) {
                navTargetNameMap.put(navTarget.getNavigationTargetName(), new ArrayList<>(1));
            }
            navTargetNameMap.get(navTarget.getNavigationTargetName()).add(navTarget);
        }
    }

    public static List<NavigationTargetOverrideInfo> forNavigationTargetName(String navTargetName) {
        try {
            return Collections.unmodifiableList(navTargetNameMap.get(valueOf(navTargetName)));
        } catch (IllegalArgumentException e) {
            log.error("unknown navigation target name:" + navTargetName + ". Should be one of " + Arrays.toString(values()));
            return Collections.emptyList();
        }
    }

}
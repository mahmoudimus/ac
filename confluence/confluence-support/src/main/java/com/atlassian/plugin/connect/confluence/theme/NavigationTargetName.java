package com.atlassian.plugin.connect.confluence.theme;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public enum NavigationTargetName {
    dashboard,
    spaceview,
    contentview;

    //just a cache for easy access (since this will be read on every load)
    private static final Map<NavigationTargetName, List<NavigationTargetOverrideInfo>> navTargetNameMap;

    static {
        navTargetNameMap = Maps.newHashMap();
        for (NavigationTargetOverrideInfo navTarget : NavigationTargetOverrideInfo.values()) {
            if (!navTargetNameMap.containsKey(navTarget.getNavigationTargetName())) {
                navTargetNameMap.put(navTarget.getNavigationTargetName(), new ArrayList<>(1));
            }
            navTargetNameMap.get(navTarget.getNavigationTargetName()).add(navTarget);
        }
    }

    public static List<NavigationTargetOverrideInfo> forNavigationTargetName(String navTargetName) {
        return navTargetNameMap.get(valueOf(navTargetName));
    }

}
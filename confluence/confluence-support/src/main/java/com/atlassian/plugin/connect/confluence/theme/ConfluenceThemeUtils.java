package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
final class ConfluenceThemeUtils {
    private static final PropertyUtilsBean BEAN_UTIL = new PropertyUtilsBean();

    private ConfluenceThemeUtils() {
    }

    public static String getThemeKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean) {
        return String.format("%s-remote-theme", bean.getKey(addon));
    }

    public static String getThemeVelocityModuleKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean) {
        return String.format("%s-remote-theme-velocity-context", bean.getKey(addon));
    }

    public static String getThemeVelocityModuleContextKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean) {
        return String.format("%s-remote-theme-velocity-context", bean.getKey(addon));
    }

    public static String getLayoutKey(ConnectAddonBean addon, ConfluenceThemeModuleBean bean, NavigationTargetOverrideInfo type) {
        return String.format("%s-remote-theme-layout-%s", bean.getKey(addon), type.name());
    }

    public static String getLayoutName(ConnectAddonBean addon, ConfluenceThemeModuleBean bean, NavigationTargetOverrideInfo type) {
        return String.format("Layout for %s type %s", bean.getKey(addon), type.name());
    }

    public static String getOverrideTypeName(final NavigationTargetOverrideInfo type) {
        return "theme-url-" + type.name();
    }

    public static ConfluenceThemeRouteBean getRouteBeanFromProperty(ConfluenceThemeRouteInterceptionsBean routes, PropertyDescriptor property) {
        try {
            Object invokeResult = property.getReadMethod().invoke(routes);
            if (invokeResult instanceof ConfluenceThemeRouteBean) {
                return (ConfluenceThemeRouteBean) invokeResult;
            } else {
                throw new IllegalStateException(String.format("expected '%s' to return a '%s', got '%s'",
                                                              property.getName(),
                                                              ConfluenceThemeRouteBean.class,
                                                              invokeResult.getClass().getName()));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            //TODO: log some sort of good error msg here? the connect bean is broken if this happens
            throw new RuntimeException(e);
        }
    }


    public static List<PropertyDescriptor> filterProperties(final ConfluenceThemeRouteInterceptionsBean props) {
        return Arrays.asList(BEAN_UTIL.getPropertyDescriptors(props))
                     .stream()
                     .filter(p -> !p.getName().equals("class")).collect(Collectors.toList());
    }
}

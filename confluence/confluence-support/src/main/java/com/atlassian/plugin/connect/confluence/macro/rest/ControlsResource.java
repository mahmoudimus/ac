package com.atlassian.plugin.connect.confluence.macro.rest;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroPropertyPanelBean;
import com.atlassian.plugin.connect.plugin.descriptor.event.EventPublishingModuleValidationExceptionHandler;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Path("/controls")
public class ControlsResource {

    private final ConnectAddonAccessor addonAccessor;
    private Consumer<Exception> moduleValidationExceptionHandler;
    private ObjectMapper objectMapper;

    public ControlsResource(ConnectAddonAccessor addonAccessor,
                            EventPublishingModuleValidationExceptionHandler moduleValidationExceptionHandler)
    {
        this.addonAccessor = addonAccessor;
        this.moduleValidationExceptionHandler = moduleValidationExceptionHandler;
        this.objectMapper = new ObjectMapper();
    }

    @Path("/{addonKey}/{macroKey}/{containerType}")
    @GET
    public Response getControls(@Context HttpServletRequest request, @PathParam("addonKey") String addonKey,
                                @PathParam("macroKey") String macroKey, @PathParam("containerType") String containerType)
    {
        Optional<ConnectAddonBean> addonBean = addonAccessor.getAddon(addonKey);
        if (!addonBean.isPresent())
        {
             return Response.status(Response.Status.NOT_FOUND).build();
        }
        Optional<List<ModuleBean>> dynamicMacros = addonBean.get().getModules().getValidModuleListOfType(
                "dynamicContentMacros", moduleValidationExceptionHandler);

        Optional<List<ControlBean>> controlBeans = retrieveControls(dynamicMacros, macroKey, containerType);
        if (controlBeans.isPresent())
        {
            try
            {
                return Response.ok().entity(objectMapper.writeValueAsString(controlBeans.get())).build();
            }
            catch(IOException exception)
            {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }


    private Optional<List<ControlBean>> retrieveControls(Optional<List<ModuleBean>> beans, String macroKey, String containerType)
    {
        if (beans.isPresent())
        {
            for (ModuleBean bean : beans.get())
            {
                DynamicContentMacroModuleBean macroBean = (DynamicContentMacroModuleBean) bean;
                if (macroKey.equals(macroBean.getRawKey()))
                {
                    if (containerType.equals("property-panel"))
                    {
                        return getControls(getPropertyPanel(macroBean));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<MacroPropertyPanelBean> getPropertyPanel(BaseContentMacroModuleBean macroBean)
    {
        return Optional.of(macroBean.getPropertyPanel());
    }

    private Optional<List<ControlBean>> getControls(Optional<MacroPropertyPanelBean> propertyPanelBean)
    {
        return propertyPanelBean.map(propertyPanel -> propertyPanel.getControls());
    }
}

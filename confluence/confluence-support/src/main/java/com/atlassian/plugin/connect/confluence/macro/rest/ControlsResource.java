package com.atlassian.plugin.connect.confluence.macro.rest;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroPropertyPanelBean;
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

@Path("/controls")
public class ControlsResource {

    private final ConnectAddonAccessor addonAccessor;
    private ObjectMapper objectMapper;

    public ControlsResource(ConnectAddonAccessor addonAccessor)
    {
        this.addonAccessor = addonAccessor;
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

        Optional<List<DynamicContentMacroModuleBean>> dynamicMacros = addonBean.get().getModules()
                .getValidModuleListOfType(DynamicContentMacroModuleBean.class, (ex) -> {});

        Optional<List<ControlBean>> controlBeans = retrievePropertyPanelControls(dynamicMacros, macroKey);

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


    private Optional<List<ControlBean>> retrievePropertyPanelControls(Optional<List<DynamicContentMacroModuleBean>> beans, String macroKey)
    {
        return getControls(getPropertyPanel(retrieveChosenMacro(beans, macroKey)));
    }

    private Optional<DynamicContentMacroModuleBean> retrieveChosenMacro(Optional<List<DynamicContentMacroModuleBean>> beans, String macroKey)
    {
        return beans.flatMap(macroBeans -> macroBeans.stream().filter(macro -> macro.getRawKey().equals(macroKey)).findFirst());
    }

    private Optional<MacroPropertyPanelBean> getPropertyPanel(Optional<DynamicContentMacroModuleBean> macroBean)
    {
        return macroBean.map(DynamicContentMacroModuleBean::getPropertyPanel);
    }

    private Optional<List<ControlBean>> getControls(Optional<MacroPropertyPanelBean> propertyPanelBean)
    {
        return propertyPanelBean.map(MacroPropertyPanelBean::getControls);
    }
}

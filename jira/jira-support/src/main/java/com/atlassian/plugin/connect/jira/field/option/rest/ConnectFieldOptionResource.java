package com.atlassian.plugin.connect.jira.field.option.rest;

import java.util.List;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.rest.util.ResponseFactory;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionService;
import com.atlassian.sal.api.message.I18nResolver;

import static java.util.stream.Collectors.toList;

@Path ("jira/addon/{addonKey}/field/{fieldKey}/option")
@Produces ("application/json")
@Consumes ("application/json")
public class ConnectFieldOptionResource
{
    private final ConnectFieldOptionService connectFieldOptionService;
    private final ResponseFactory responseFactory;
    private final ConnectFieldOptionBeansFactory beansFactory;

    public ConnectFieldOptionResource(final ConnectFieldOptionService connectFieldOptionService, final ResponseFactory responseFactory, final ConnectFieldOptionBeansFactory connectFieldOptionBeansFactory)
    {
        this.connectFieldOptionService = connectFieldOptionService;
        this.responseFactory = responseFactory;
        this.beansFactory = connectFieldOptionBeansFactory;
    }

    @GET
    public Response getAllOptions(@PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest)
    {
        ServiceOutcome<List<ConnectFieldOption>> allOptions = connectFieldOptionService.getAllOptions(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey));

        return toEither(allOptions).left().on(options -> {
            List<ConnectFieldOptionBean> result = options.stream()
                    .map(beansFactory::toBean)
                    .collect(toList());

            return responseFactory.okNoCache(result);
        });
    }

    @GET
    @Path("/{optionId}")
    public Response getOption(@PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest)
    {
        ServiceOutcome<ConnectFieldOption> option = connectFieldOptionService.getOption(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), optionId);
        return toEither(option)
                .right().map(beansFactory::toBean)
                .left().on(responseFactory::okNoCache);
    }

    @POST
    public Response createOption(ConnectFieldOptionBean connectFieldOptionBean, @PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest)
    {
        return beansFactory.jsonFromBean(connectFieldOptionBean)
                .left().map(responseFactory::errorResponse)
                .left().on(json -> {
                    ServiceOutcome<ConnectFieldOption> createResult = connectFieldOptionService.addOption(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), json);
                    return toEither(createResult).left().on(option -> responseFactory.okNoCache(beansFactory.toBean(option)));
                });
    }

    @PUT
    @Path("/{optionId}")
    public Response putOption(ConnectFieldOptionBean connectFieldOptionBean, @PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest)
    {
        return beansFactory.fromBean(optionId, connectFieldOptionBean)
                .left().map(responseFactory::errorResponse)
                .left().on(connectFieldOption -> {
                    ServiceOutcome<ConnectFieldOption> createResult = connectFieldOptionService.putOption(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), connectFieldOption);
                    return toEither(createResult).left().on(resultOption -> responseFactory.okNoCache(beansFactory.toBean(resultOption)));
                });
    }

    @DELETE
    @Path("/{optionId}")
    public Response delete(@PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest)
    {
        ServiceResult deleteResult = connectFieldOptionService.removeOption(
                AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), optionId);

        return map(deleteResult, responseFactory::noContent);
    }

    @POST
    @Path("/replace")
    public Response replace(ReplaceRequestBean replaceRequestBean, @PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest)
    {
        ServiceResult replaceResult = connectFieldOptionService.replaceInAllIssues(
                AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), replaceRequestBean.getFrom(), replaceRequestBean.getTo());
        return map(replaceResult, () -> Response.ok().cacheControl(CacheControl.never()).build());
    }

    private <T> Either<Response, T> toEither(ServiceOutcome<T> outcome)
    {
        return outcome.isValid() ? Either.right(outcome.get()) : Either.left(responseFactory.errorResponse(outcome.getErrorCollection()));
    }

    private Response map(ServiceResult outcome, Supplier<Response> actionIfValid)
    {
        return outcome.isValid() ? actionIfValid.get() : responseFactory.errorResponse(outcome.getErrorCollection());
    }
}

package com.atlassian.plugin.connect.jira.field.option.rest;

import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.rest.api.pagination.PageBean;
import com.atlassian.jira.rest.util.ResponseFactory;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.connect.api.auth.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionService;
import com.atlassian.plugin.connect.jira.util.ServiceOutcomes;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.function.Supplier;

@Path("jira/addon/{addonKey}/field/{fieldKey}/option")
@Produces("application/json")
@Consumes("application/json")
public class ConnectFieldOptionResource {
    private final ConnectFieldOptionService connectFieldOptionService;
    private final ResponseFactory responseFactory;
    private final ConnectFieldOptionBeansFactory beansFactory;
    private final JiraBaseUrls jiraBaseUrls;
    private final I18nResolver i18;

    public ConnectFieldOptionResource(final ConnectFieldOptionService connectFieldOptionService, final ResponseFactory responseFactory, final ConnectFieldOptionBeansFactory connectFieldOptionBeansFactory, final JiraBaseUrls jiraBaseUrls, final I18nResolver i18) {
        this.connectFieldOptionService = connectFieldOptionService;
        this.responseFactory = responseFactory;
        this.beansFactory = connectFieldOptionBeansFactory;
        this.jiraBaseUrls = jiraBaseUrls;
        this.i18 = i18;
    }

    @GET
    public Response getAllOptions(
            @QueryParam("startAt") final Long startAt, @QueryParam("maxResults") final Integer maxResults,
            @PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest) {
        PageRequest pageRequest = PageRequests.request(startAt, maxResults);

        ServiceOutcome<Page<ConnectFieldOption>> allOptions = connectFieldOptionService.getOptions(
                AuthenticationData.byRequest(servletRequest),
                FieldId.of(addonKey, fieldKey),
                pageRequest);

        return optionsToResponse(servletRequest, pageRequest, allOptions);
    }

    @GET
    @Path("/scoped")
    public Response getScopedOptions(
            @QueryParam ("startAt") final Long startAt, @QueryParam ("maxResults") final Integer maxResults, @QueryParam("projectId") Long projectId,
            @PathParam ("addonKey") String addonKey, @PathParam ("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest)
    {
        PageRequest pageRequest = PageRequests.request(startAt, maxResults);

        ServiceOutcome<Page<ConnectFieldOption>> allOptions = connectFieldOptionService.getOptions(
                AuthenticationData.byRequest(servletRequest),
                FieldId.of(addonKey, fieldKey),
                pageRequest,
                ConnectFieldOptionScope.builder().setProjectId(projectId).build());

        return optionsToResponse(servletRequest, pageRequest, allOptions);
    }

    private Response optionsToResponse(final @Context HttpServletRequest servletRequest, final PageRequest pageRequest, final ServiceOutcome<Page<ConnectFieldOption>> allOptions)
    {
        return toEither(allOptions)
                .right().map(options -> PageBean
                        .from(pageRequest, options)
                        .setLinks(UriBuilder.fromPath(jiraBaseUrls.baseUrl() + servletRequest.getServletPath()).build().toString(), pageRequest.getLimit())
                        .build(beansFactory::toBean))
                .left().on(responseFactory::okNoCache);
    }

    @GET
    @Path("/{optionId}")
    public Response getOption(@PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest) {
        ServiceOutcome<ConnectFieldOption> option = connectFieldOptionService.getOption(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), optionId);
        return toEither(option)
                .right().map(beansFactory::toBean)
                .left().on(responseFactory::okNoCache);
    }

    @POST
    public Response createOption(ConnectFieldOptionBean connectFieldOptionBean, @PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest) {
        return beansFactory.jsonFromBean(connectFieldOptionBean)
                .left().map(responseFactory::errorResponse)
                .left().on(json -> {
                    AuthenticationData.Request auth = AuthenticationData.byRequest(servletRequest);
                    FieldId fieldId = FieldId.of(addonKey, fieldKey);
                    ConnectFieldOptionScope scope = Objects.firstNonNull(beansFactory.fromBean(connectFieldOptionBean.getScope()), ConnectFieldOptionScope.GLOBAL);
                    ServiceOutcome<ConnectFieldOption> createResult = connectFieldOptionService.addOption(auth, fieldId, json, scope);
                    return toEither(createResult).left().on(option -> responseFactory.okNoCache(beansFactory.toBean(option)));
                });
    }

    @PUT
    @Path("/{optionId}")
    public Response putOption(ConnectFieldOptionBean connectFieldOptionBean, @PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest) {
        return beansFactory.fromBean(optionId, connectFieldOptionBean)
                .left().map(responseFactory::errorResponse)
                .left().on(connectFieldOption -> {
                    ServiceOutcome<ConnectFieldOption> createResult = connectFieldOptionService.putOption(AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), connectFieldOption);
                    return toEither(createResult).left().on(resultOption -> responseFactory.okNoCache(beansFactory.toBean(resultOption)));
                });
    }

    @DELETE
    @Path("/{optionId}")
    public Response delete(@PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @PathParam("optionId") Integer optionId, @Context HttpServletRequest servletRequest) {
        ServiceResult deleteResult = connectFieldOptionService.removeOption(
                AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), optionId);

        return map(deleteResult, responseFactory::noContent);
    }

    @POST
    @Path("/replace")
    public Response replace(ReplaceRequestBean replaceRequestBean, @PathParam("addonKey") String addonKey, @PathParam("fieldKey") String fieldKey, @Context HttpServletRequest servletRequest) {
        if (replaceRequestBean.getFrom() == null || replaceRequestBean.getTo() == null) {
            return responseFactory.badRequest(i18.getText("connect.issue.field.option.rest.replace.fields.required"));
        }

        ServiceResult replaceResult = connectFieldOptionService.replaceInAllIssues(
                AuthenticationData.byRequest(servletRequest), FieldId.of(addonKey, fieldKey), replaceRequestBean.getFrom(), replaceRequestBean.getTo());
        return map(replaceResult, () -> Response.ok().cacheControl(CacheControl.never()).build());
    }

    private <T> Either<Response, T> toEither(ServiceOutcome<T> outcome) {
        return ServiceOutcomes.toEither(outcome).left().map(responseFactory::errorResponse);
    }

    private Response map(ServiceResult outcome, Supplier<Response> actionIfValid) {
        return outcome.isValid() ? actionIfValid.get() : responseFactory.errorResponse(outcome.getErrorCollection());
    }
}

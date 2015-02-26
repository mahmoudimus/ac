package it.com.atlassian.plugin.connect.scopes.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class JiraScopeTestUtil
{
    public static final String ADMIN_USERNAME = "admin";

    private final UserManager userManager;
    private final ProjectService projectService;
    private final CommentService commentService;
    private final CommentPropertyService commentPropertyService;
    private final IssueService issueService;

    public JiraScopeTestUtil(final UserManager userManager,
            final ProjectService projectService,
            final CommentService commentService,
            final CommentPropertyService commentPropertyService,
            final IssueService issueService)
    {
        this.userManager = userManager;
        this.projectService = projectService;
        this.commentService = commentService;
        this.commentPropertyService = commentPropertyService;
        this.issueService = issueService;
    }

    public Project createProject() throws IOException
    {
        int keyLength = 6;
        String key = RandomStringUtils.randomAlphabetic(keyLength).toUpperCase();
        ApplicationUser user = getAdmin();
        ProjectService.CreateProjectValidationResult result = projectService.validateCreateProject(
                user.getDirectoryUser(), key, key, null, ADMIN_USERNAME, null, AssigneeTypes.PROJECT_LEAD);
        return projectService.createProject(result);
    }

    public ApplicationUser getAdmin() {return userManager.getUserByKey(ADMIN_USERNAME);}

    public Comment createComment() throws JSONException, IOException
    {
        final ApplicationUser admin = getAdmin();
        final User user = getAdmin().getDirectoryUser();

        Project project = createProject();

        IssueService.IssueResult issueResult = issueService.create(user, issueService.validateCreate(user, issueService.newIssueInputParameters()
                .setIssueTypeId("1")
                .setAssigneeId(admin.getKey())
                .setDescription("Description")
                .setProjectId(project.getId())
                .setSummary("Summary")));

        final CommentService.CommentCreateValidationResult validationResult = commentService.validateCommentCreate(admin, CommentService.CommentParameters.builder()
                .issue(issueResult.getIssue())
                .body("comment")
                .build());

        return commentService.create(admin, validationResult, false);
    }

    public EntityProperty createCommentProperty(final Comment comment)
    {
        EntityPropertyService.PropertyInput property = new EntityPropertyService.PropertyInput(new JSONObject(ImmutableMap.of("x", "y")).toString(), "key");
        EntityPropertyService.SetPropertyValidationResult validationResult = commentPropertyService.validateSetProperty(getAdmin(), comment.getId(), property);
        EntityPropertyService.PropertyResult propertyResult = commentPropertyService.setProperty(getAdmin(), validationResult);
        assertTrue(propertyResult.isValid());
        return propertyResult.getEntityProperty().get();
    }
}

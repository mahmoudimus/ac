package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.manager.webhook.InvalidWebhookEndpointException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.model.webhook.Webhook;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.component.ComponentLocator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ConfluenceComponent
public class ConfluenceApplicationService implements ApplicationService
{
    private  final ApplicationService delegate;

    public ConfluenceApplicationService()
    {
        delegate = ComponentLocator.getComponent(ApplicationService.class);
    }

    @Override
    public User authenticateUser(Application application, String username, PasswordCredential passwordCredential) throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        return delegate.authenticateUser(application, username, passwordCredential); // TODO CONFDEV-22477: remove when we can @ComponentImport ApplicationService
    }

    @Override
    public boolean isUserAuthorised(Application application, String username)
    {
        return delegate.isUserAuthorised(application, username);
    }

    @Override
    public void addAllUsers(Application application, Collection<UserTemplateWithCredentialAndAttributes> users) throws ApplicationPermissionException, OperationFailedException, BulkAddFailedException
    {
        delegate.addAllUsers(application, users);
    }

    @Override
    public User findUserByName(Application application, String name) throws UserNotFoundException
    {
        return delegate.findUserByName(application, name);
    }

    @Override
    public UserWithAttributes findUserWithAttributesByName(Application application, String name) throws UserNotFoundException
    {
        return delegate.findUserWithAttributesByName(application, name);
    }

    @Override
    public User addUser(Application application, UserTemplate user, PasswordCredential credential) throws InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        return delegate.addUser(application, user, credential);
    }

    @Override
    public User updateUser(Application application, UserTemplate user) throws InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        return delegate.updateUser(application, user);
    }

    @Override
    public User renameUser(Application application, String oldUserName, String newUsername) throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidUserException
    {
        return delegate.renameUser(application, oldUserName, newUsername);
    }

    @Override
    public void updateUserCredential(Application application, String username, PasswordCredential credential) throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException
    {
        delegate.updateUserCredential(application, username, credential);
    }

    @Override
    public void storeUserAttributes(Application application, String username, Map<String, Set<String>> attributes) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        delegate.storeUserAttributes(application, username, attributes);
    }

    @Override
    public void removeUserAttributes(Application application, String username, String attributeName) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        delegate.removeUserAttributes(application, username, attributeName);
    }

    @Override
    public void removeUser(Application application, String user) throws OperationFailedException, UserNotFoundException, ApplicationPermissionException
    {
        delegate.removeUser(application, user);
    }

    @Override
    public <T> List<T> searchUsers(Application application, EntityQuery<T> query)
    {
        return delegate.searchUsers(application, query);
    }

    @Override
    @Deprecated
    public List<User> searchUsersAllowingDuplicateNames(Application application, EntityQuery<User> query)
    {
        return delegate.searchUsersAllowingDuplicateNames(application, query);
    }

    @Override
    public Group findGroupByName(Application application, String name) throws GroupNotFoundException
    {
        return delegate.findGroupByName(application, name);
    }

    @Override
    public GroupWithAttributes findGroupWithAttributesByName(Application application, String name) throws GroupNotFoundException
    {
        return delegate.findGroupWithAttributesByName(application, name);
    }

    @Override
    public Group addGroup(Application application, GroupTemplate group) throws InvalidGroupException, OperationFailedException, ApplicationPermissionException
    {
        return delegate.addGroup(application, group);
    }

    @Override
    public Group updateGroup(Application application, GroupTemplate group) throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        return delegate.updateGroup(application, group);
    }

    @Override
    public void storeGroupAttributes(Application application, String groupname, Map<String, Set<String>> attributes) throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        delegate.storeGroupAttributes(application, groupname, attributes);
    }

    @Override
    public void removeGroupAttributes(Application application, String groupname, String attributeName) throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        delegate.removeGroupAttributes(application, groupname, attributeName);
    }

    @Override
    public void removeGroup(Application application, String group) throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException
    {
        delegate.removeGroup(application, group);
    }

    @Override
    public <T> List<T> searchGroups(Application application, EntityQuery<T> query)
    {
        return delegate.searchGroups(application, query);
    }

    @Override
    public void addUserToGroup(Application application, String username, String groupName) throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException, MembershipAlreadyExistsException
    {
        delegate.addUserToGroup(application, username, groupName);
    }

    @Override
    public void addGroupToGroup(Application application, String childGroupName, String parentGroupName) throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, InvalidMembershipException, MembershipAlreadyExistsException
    {
        delegate.addGroupToGroup(application, childGroupName, parentGroupName);
    }

    @Override
    public void removeUserFromGroup(Application application, String username, String groupName) throws OperationFailedException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        delegate.removeUserFromGroup(application, username, groupName);
    }

    @Override
    public void removeGroupFromGroup(Application application, String childGroup, String parentGroup) throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        delegate.removeGroupFromGroup(application, childGroup, parentGroup);
    }

    @Override
    public boolean isUserDirectGroupMember(Application application, String username, String groupName)
    {
        return delegate.isUserDirectGroupMember(application, username, groupName);
    }

    @Override
    public boolean isGroupDirectGroupMember(Application application, String childGroup, String parentGroup)
    {
        return delegate.isGroupDirectGroupMember(application, childGroup, parentGroup);
    }

    @Override
    public boolean isUserNestedGroupMember(Application application, String username, String groupName)
    {
        return delegate.isUserNestedGroupMember(application, username, groupName);
    }

    @Override
    public boolean isGroupNestedGroupMember(Application application, String childGroup, String parentGroup)
    {
        return delegate.isGroupNestedGroupMember(application, childGroup, parentGroup);
    }

    @Override
    public <T> List<T> searchDirectGroupRelationships(Application application, MembershipQuery<T> query)
    {
        return delegate.searchDirectGroupRelationships(application, query);
    }

    @Override
    public <T> List<T> searchNestedGroupRelationships(Application application, MembershipQuery<T> query)
    {
        return delegate.searchNestedGroupRelationships(application, query);
    }

    @Override
    public String getCurrentEventToken(Application application) throws IncrementalSynchronisationNotAvailableException
    {
        return delegate.getCurrentEventToken(application);
    }

    @Override
    public Events getNewEvents(Application application, String eventToken) throws EventTokenExpiredException, OperationFailedException
    {
        return delegate.getNewEvents(application, eventToken);
    }

    @Override
    public Webhook findWebhookById(Application application, long webhookId) throws WebhookNotFoundException, ApplicationPermissionException
    {
        return delegate.findWebhookById(application, webhookId);
    }

    @Override
    public Webhook registerWebhook(Application application, String endpointUrl, @Nullable String token) throws InvalidWebhookEndpointException
    {
        return delegate.registerWebhook(application, endpointUrl, token);
    }

    @Override
    public void unregisterWebhook(Application application, long webhookId) throws ApplicationPermissionException, WebhookNotFoundException
    {
        delegate.unregisterWebhook(application, webhookId);
    }
}

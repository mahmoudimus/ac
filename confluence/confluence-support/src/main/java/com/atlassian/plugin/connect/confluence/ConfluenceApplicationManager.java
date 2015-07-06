package com.atlassian.plugin.connect.confluence;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.component.ComponentLocator;

import java.util.List;
import java.util.Set;

@ConfluenceComponent
@ExportAsDevService
public class ConfluenceApplicationManager implements ApplicationManager
{
    private final ApplicationManager delegate;

    public ConfluenceApplicationManager()
    {
        this.delegate = ComponentLocator.getComponent(ApplicationManager.class); // TODO CONFDEV-22477: remove when we can @ComponentImport ApplicationManager
    }

    @Override
    public Application add(Application application) throws InvalidCredentialException
    {
        return delegate.add(application);
    }

    @Override
    public Application findById(long id) throws ApplicationNotFoundException
    {
        return delegate.findById(id);
    }

    @Override
    public Application findByName(String name) throws ApplicationNotFoundException
    {
        return delegate.findByName(name);
    }

    @Override
    public void remove(Application application) throws ApplicationManagerException
    {
        delegate.remove(application);
    }

    @Override
    public void removeDirectoryFromApplication(Directory directory, Application application) throws ApplicationManagerException
    {
        delegate.removeDirectoryFromApplication(directory, application);
    }

    @Override
    public void addDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, OperationType... operationTypes) throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        delegate.addDirectoryMapping(application, directory, allowAllToAuthenticate, operationTypes);
    }

    @Override
    public void updateDirectoryMapping(Application application, Directory directory, int position) throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        delegate.updateDirectoryMapping(application, directory, position);
    }

    @Override
    public void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate) throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        delegate.updateDirectoryMapping(application, directory, allowAllToAuthenticate);
    }

    @Override
    public void updateDirectoryMapping(Application application, Directory directory, boolean allowAllToAuthenticate, Set<OperationType> operationTypes) throws ApplicationNotFoundException, DirectoryNotFoundException
    {
        delegate.updateDirectoryMapping(application, directory, allowAllToAuthenticate, operationTypes);
    }

    @Override
    public void addRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException
    {
        delegate.addRemoteAddress(application, remoteAddress);
    }

    @Override
    public void removeRemoteAddress(Application application, RemoteAddress remoteAddress) throws ApplicationNotFoundException
    {
        delegate.removeRemoteAddress(application, remoteAddress);
    }

    @Override
    public void addGroupMapping(Application application, Directory directory, String groupName) throws ApplicationNotFoundException
    {
        delegate.addGroupMapping(application, directory, groupName);
    }

    @Override
    public void removeGroupMapping(Application application, Directory directory, String groupName) throws ApplicationNotFoundException
    {
        delegate.removeGroupMapping(application, directory, groupName);
    }

    @Override
    public Application update(Application application) throws ApplicationManagerException, ApplicationNotFoundException
    {
        return delegate.update(application);
    }

    @Override
    public void updateCredential(Application application, PasswordCredential passwordCredential) throws ApplicationManagerException, ApplicationNotFoundException
    {
        delegate.updateCredential(application, passwordCredential);
    }

    @Override
    public boolean authenticate(Application application, PasswordCredential testCredential) throws ApplicationNotFoundException
    {
        return delegate.authenticate(application, testCredential);
    }

    @Override
    public List<Application> search(EntityQuery query)
    {
        return delegate.search(query);
    }

    @Override
    public List<Application> findAll()
    {
        return delegate.findAll();
    }
}

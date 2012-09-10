package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import org.springframework.beans.BeanUtils;

/**
 */
public class ConfluenceDomain
{
    private ConfluenceDomain()
    {}

    public static MutableAttachment newAttachment()
    {
        return new AttachmentImpl();
    }

    public static MutableAttachment newAttachment(Attachment attachment)
    {
        return copy(attachment, new AttachmentImpl());
    }

    public static MutableComment newComment()
    {
        return new CommentImpl();
    }

    public static MutableComment newComment(Comment comment)
    {
        return copy(comment, new CommentImpl());
    }

    public static MutableBlogEntry newBlogPost()
    {
        return new BlogEntryImpl();
    }

    public static MutableBlogEntry newBlogPost(BlogEntry blogEntry)
    {
        return copy(blogEntry, new BlogEntryImpl());
    }

    public static MutableContentPermission newContentPermission()
    {
        return new ContentPermissionImpl();
    }

    public static MutableContentPermission newContentPermission(ContentPermission contentPermission)
    {
        return copy(contentPermission, new ContentPermissionImpl());
    }

    public static MutableLabel newLabel()
    {
        return new LabelImpl();
    }

    public static MutableLabel newLabel(Label label)
    {
        return copy(label, new LabelImpl());
    }

    public static MutablePage newPage()
    {
        return new PageImpl();
    }

    public static MutablePage newPage(Page page)
    {
        return copy(page, new PageImpl());
    }

    public static PageUpdateOptions newPageUpdateOptions()
    {
        return new PageUpdateOptionsImpl();
    }

    public static RenderOptions newRenderOptions()
    {
        return new RenderOptionsImpl();
    }

    public static SearchOptions newSearchOptions()
    {
        return new SearchOptionsImpl();
    }

    public static MutableSpace newSpace()
    {
        return new SpaceImpl();
    }

    public static MutableSpace newSpace(Space space)
    {
        return copy(space, new SpaceImpl());
    }

    public static MutableUser newUser()
    {
        return new UserImpl();
    }

    public static MutableUser newUser(User user)
    {
        return copy(user, new UserImpl());
    }

    public static MutableUserInformation newUserInformation()
    {
        return new UserInformationImpl();
    }

    public static MutableUserInformation newUserInformation(UserInformation userInformation)
    {
        return copy(userInformation, new UserInformationImpl());
    }

    private static <S, T> T copy(S source, T target)
    {
        BeanUtils.copyProperties(source, target);
        return target;
    }
}

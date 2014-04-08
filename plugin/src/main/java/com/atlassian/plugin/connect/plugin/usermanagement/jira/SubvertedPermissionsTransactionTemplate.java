package com.atlassian.plugin.connect.plugin.usermanagement.jira;

import com.atlassian.jira.util.ImportUtils;
import com.atlassian.sal.api.transaction.TransactionCallback;

/**
 * Subverts JIRA permission checks while executing the supplied transaction callback.
 *
 * @since 1.0.1
 */
public class SubvertedPermissionsTransactionTemplate<T> implements TransactionCallback<T>
{
    private final TransactionCallback<T> delegate;

    public SubvertedPermissionsTransactionTemplate(final TransactionCallback<T> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public T doInTransaction()
    {
        final boolean originalSubvertState = ImportUtils.isSubvertSecurityScheme();
        try {
            ImportUtils.setSubvertSecurityScheme(true);
            return delegate.doInTransaction();
        } finally
        {
            ImportUtils.setSubvertSecurityScheme(originalSubvertState);
        }
    }

    public static <T> SubvertedPermissionsTransactionTemplate<T> subvertPermissions(TransactionCallback<T> callback)
    {
        return new SubvertedPermissionsTransactionTemplate<T>(callback);
    }
}

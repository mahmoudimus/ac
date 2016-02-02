package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions.newDialogOptions;
import static com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogSize.medium;
import static org.mockito.Mockito.mock;

public class DialogOptionsValidatorTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DialogOptionsValidator validator = new DialogOptionsValidator(mock(ShallowConnectAddonBean.class), mock(ConnectModuleMeta.class));

    @Test
    public void testValidateEmptyOptions() throws Exception
    {
        DialogOptions options = newDialogOptions().build();

        validator.validate(options);
    }

    @Test
    public void testValidateOptionsWithSize() throws Exception
    {
        DialogOptions options = newDialogOptions()
                .withSize(medium)
                .build();

        validator.validate(options);
    }

    @Test
    public void testValidateOptionsWithSizeAndOneDimension() throws Exception
    {
        DialogOptions options = newDialogOptions()
                .withSize(medium)
                .withHeight("100")
                .build();

        validator.validate(options);
    }

    @Test
    public void testValidateOptionsWithSizeAndTwoDimensions() throws Exception
    {
        DialogOptions options = newDialogOptions()
                .withSize(medium)
                .withHeight("100")
                .withWidth("100")
                .build();

        validator.validate(options);
    }

    @Test
    public void testValidateOptionsWithNoSizeAndOneDimension() throws Exception
    {
        thrown.expect(ConnectModuleValidationException.class);

        DialogOptions options = newDialogOptions()
                .withHeight("100")
                .build();

        validator.validate(options);
    }

    @Test
    public void testValidateOptionsWithNoSizeAndTwoDimensions() throws Exception
    {
        DialogOptions options = newDialogOptions()
                .withHeight("100")
                .withWidth("100")
                .build();

        validator.validate(options);
    }

    @Test
    public void testValidDimensions() throws Exception
    {
        validator.validateDimension("100%");
        validator.validateDimension("100px");
        validator.validateDimension("100");
    }

    @Test
    public void testInvalidDimensionZero() throws Exception
    {
        invalidDimension("0");
    }

    @Test
    public void testInvalidDimension100Percent() throws Exception
    {
        invalidDimension("110%");
    }

    @Test
    public void testInvalidDimensionNegative() throws Exception
    {
        invalidDimension("-1");
    }

    @Test
    public void testInvalidDimensionBadSuffix() throws Exception
    {
        invalidDimension("1024pt");
    }

    @Test
    public void testInvalidDimensionBadPrefix() throws Exception
    {
        invalidDimension("ohnoyoudont100");
    }

    @Test
    public void testInvalidDimensionNotAnIntSmartyPants() throws Exception
    {
        invalidDimension(String.valueOf((long)Integer.MAX_VALUE + 1));
    }

    private void invalidDimension(String dimension) throws ConnectModuleValidationException
    {
        thrown.expect(ConnectModuleValidationException.class);
        validator.validateDimension(dimension);
    }
}
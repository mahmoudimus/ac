package com.atlassian.plugin.connect.util.auth;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.writer.JwtClaimWriter;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtJsonBuilderFactory;

public class TestJwtJsonBuilderFactory implements JwtJsonBuilderFactory
{
    private final List<JwtClaimWriter> claimWriters;

    public TestJwtJsonBuilderFactory(JwtClaimWriter ...claimWriters)
    {
        this.claimWriters = Arrays.asList(claimWriters);
    }

    @Nonnull
    @Override
    public JwtJsonBuilder jsonBuilder()
    {
        JwtJsonBuilder builder = new JsonSmartJwtJsonBuilder();
        for (JwtClaimWriter claimWriter : claimWriters)
        {
            claimWriter.write(builder);
        }
        return builder;
    }
}

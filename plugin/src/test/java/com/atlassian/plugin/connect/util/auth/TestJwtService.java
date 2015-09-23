package com.atlassian.plugin.connect.util.auth;

import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.JwtService;
import com.atlassian.jwt.applinks.exception.NotAJwtPeerException;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtSigningException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.writer.JwtWriterFactory;

import java.util.Map;

import javax.annotation.Nonnull;

public class TestJwtService implements JwtService
{
    private JwtWriterFactory jwtWriterFactory;

    public TestJwtService()
    {
        this.jwtWriterFactory = new NimbusJwtWriterFactory();
    }

    @Nonnull
    @Override
    public Jwt verifyJwt(@Nonnull String jwt, @Nonnull Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws NotAJwtPeerException, JwtParseException, JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
    {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public String issueJwt(@Nonnull String jsonPayload, @Nonnull String secret) throws NotAJwtPeerException, JwtSigningException
    {
        return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, secret).jsonToJwt(jsonPayload);
    }

    @Nonnull
    @Override
    public String issueJwt(@Nonnull String jsonPayload, @Nonnull String secret, SigningAlgorithm algorithm)
    {
        return jwtWriterFactory.macSigningWriter(algorithm, secret).jsonToJwt(jsonPayload);
    }
}

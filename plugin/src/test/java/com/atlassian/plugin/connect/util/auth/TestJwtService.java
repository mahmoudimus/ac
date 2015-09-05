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
    private final String sharedSecret;
    private JwtWriterFactory jwtWriterFactory;

    public TestJwtService(String sharedSecret)
    {
        this.sharedSecret = sharedSecret;
        this.jwtWriterFactory = new NimbusJwtWriterFactory();
    }

    @Override
    public Jwt verifyJwt(String jwt, Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws NotAJwtPeerException, JwtParseException, JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String issueJwt(String jsonPayload, String secret) throws NotAJwtPeerException, JwtSigningException
    {
        return issueJwt(jsonPayload, secret, SigningAlgorithm.HS256);
    }

    @Nonnull
    @Override
    public String issueJwt(@Nonnull String jsonPayload, @Nonnull String secret, SigningAlgorithm algorithm)
    {
        return jwtWriterFactory.macSigningWriter(algorithm, sharedSecret).jsonToJwt(jsonPayload);
    }
}

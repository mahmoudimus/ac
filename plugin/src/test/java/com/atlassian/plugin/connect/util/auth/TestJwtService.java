package com.atlassian.plugin.connect.util.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.applinks.ApplinkJwt;
import com.atlassian.jwt.applinks.JwtService;
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
    public boolean isJwtPeer(ApplicationLink applicationLink)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplinkJwt verifyJwt(String jwt, Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws NotAJwtPeerException, JwtParseException, JwtVerificationException, TypeNotInstalledException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String issueJwt(String jsonPayload, ApplicationLink applicationLink) throws NotAJwtPeerException, JwtSigningException
    {
        return issueJwt(jsonPayload, sharedSecret);
    }

    @Override
    public String issueJwt(String jsonPayload, String secret) throws NotAJwtPeerException, JwtSigningException
    {
        return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, sharedSecret).jsonToJwt(jsonPayload);
    }}

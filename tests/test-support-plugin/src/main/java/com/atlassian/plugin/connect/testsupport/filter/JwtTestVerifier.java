package com.atlassian.plugin.connect.testsupport.filter;

import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.core.reader.JwtClaimEqualityVerifier;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.exception.*;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

public class JwtTestVerifier
{
    private final String sharedSecret;
    private final String clientId;
    private final NimbusJwtReaderFactory readerFactory;

    public JwtTestVerifier(String sharedSecret, String clientId)
    {
        this.sharedSecret = sharedSecret;
        this.clientId = clientId;
        this.readerFactory = new NimbusJwtReaderFactory(new TestJwtIssuerValidator(clientId),new TestJwtIssuerSharedSecretService(sharedSecret));
    }
    
    public Jwt getVerifiedJwt(String tokenHeader) throws JwtParseException, JwtUnknownIssuerException, JwtVerificationException, JwtIssuerLacksSharedSecretException
    {
        if(!tokenHeader.startsWith(JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX))
        {
            throw new JwtParseException("authorization header does not start with " + JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX);
        }
        
        String token = StringUtils.substringAfter(tokenHeader, JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX);
        
        return readerFactory.getReader(token).read(token, Maps.<String, JwtClaimEqualityVerifier> newHashMap());
    }

    public boolean jwtAndClientAreValid(String tokenHeader) throws JwtParseException, JwtUnknownIssuerException, JwtVerificationException, JwtIssuerLacksSharedSecretException
    {
        return clientId.equals(getVerifiedJwt(tokenHeader).getIssuer());
    }

    public class TestJwtIssuerValidator implements JwtIssuerValidator
    {
        private final String issuerId;

        public TestJwtIssuerValidator(String issuerId)
        {
            this.issuerId = issuerId;
        }

        @Override
        public boolean isValid(String s)
        {
            return issuerId.equals(s);
        }
    }

    public class TestJwtIssuerSharedSecretService implements JwtIssuerSharedSecretService
    {
        private final String secret;

        public TestJwtIssuerSharedSecretService(String secret)
        {
            this.secret = secret;
        }

        @Override
        public String getSharedSecret(String s) throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
        {
            return secret;
        }
    }
}

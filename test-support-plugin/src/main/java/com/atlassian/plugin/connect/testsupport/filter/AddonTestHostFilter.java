package com.atlassian.plugin.connect.testsupport.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.reader.JwtReader;
import com.atlassian.jwt.reader.JwtReaderFactory;
//import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record incoming requests that are from "me" as part of add-on wired tests.
 * Do not pass these requests on to subsequent filters.
 * Pass all other requests on through the filter chain.
 * Also see {@link com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterBase}.
 */
public class AddonTestHostFilter extends AddonTestFilterBase
{
    private final JwtReaderFactory jwtReaderFactory;

    private static final Logger log = LoggerFactory.getLogger(AddonTestHostFilter.class);

    public AddonTestHostFilter(AddonTestFilterResults testFilterResults, JwtReaderFactory jwtReaderFactory,
                               UserManager userManager)
    {
        super(testFilterResults, userManager);
        this.jwtReaderFactory = jwtReaderFactory;
    }

    @Override
    protected boolean shouldProcess(HttpServletRequest request)
    {
        String jwtToken = JwtUtil.extractJwt(request);

        if (!StringUtils.isEmpty(jwtToken))
        {
            try
            {
                JwtReader jwtReader = jwtReaderFactory.getReader(jwtToken);
                Jwt decodedToken = jwtReader.read(jwtToken, Collections.<String, JwtClaimVerifier>emptyMap());
                return jwtWasIssuedByHost(decodedToken.getIssuer());
            }
            catch (JwtUnknownIssuerException e)
            {
                return jwtWasIssuedByHost(e.getMessage());
            }
            catch (Exception e)
            {
                // one of the many possible JWT reading exceptions was thrown - log for debugging and let the invoking test fail
                log.error(String.format("Failed to read JWT token '%s' due to exception: ", jwtToken), e);
            }
        }

        return false;
    }

    private boolean jwtWasIssuedByHost(String issuer)
    {
        return issuer.startsWith("jira"); // TODO: Not sure how to implement this properly w/o oauth
//        return consumerService.getConsumer().getKey().equals(issuer);
    }

    @Override
    protected void processNonMatch(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        filterChain.doFilter(request, response);
    }
}

package com.atlassian.plugin.connect.plugin.auth.oauth;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.plugin.connect.plugin.auth.DefaultMessage;
import com.atlassian.sal.api.auth.AuthenticationListener;
import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

@Component
public class ThreeLeggedOAuthFilter implements Filter {
	
	private UserManager userManager;
	private AuthenticationListener authenticationListener;
	private static final String SECRET = "sphere";
	
	@Autowired
	public ThreeLeggedOAuthFilter(UserManager userManager, AuthenticationListener authenticationListener) {
		this.userManager = userManager;
		this.authenticationListener = authenticationListener;
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
		String authzHeader = request.getHeader("Authorization");
		System.out.println("found authz header: " + authzHeader);
		
		if(authzHeader != null && authzHeader.toUpperCase().startsWith("BEARER")) {
			String token = authzHeader.split(" ")[1].trim();
			byte[] decoded = Base64.decode(token);
			String userName = new String(decoded, "UTF-8").split(":")[0];
			UserProfile userProfile = this.userManager.getUserProfile(userName);
			
	        SimplePrincipal principal = new SimplePrincipal(userProfile.getUsername());
	        final Authenticator.Result authenticationResult = new Authenticator.Result.Success(new DefaultMessage("YOLO"), principal);
	        authenticationListener.authenticationSuccess(authenticationResult, request, response);	
		}
		chain.doFilter(request, response);

	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
	}

}

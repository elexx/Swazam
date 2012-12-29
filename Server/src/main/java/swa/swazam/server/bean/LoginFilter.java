package swa.swazam.server.bean;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginFilter implements Filter {

	@Override
	public void destroy() {}

	@Override
	/**
	 * Implements a filter. Every page that a user requests is processed by this filter and only if the user
	 * is already logged in, he is allowed to access certain pages. In case the user is not logged in and requests a
	 * page he should not see, he is redirected to the login page.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest req = (HttpServletRequest) request;
	    LoginBean loginBean = (LoginBean) req.getSession().getAttribute("loginBean");
	   
	    String path = req.getRequestURI().substring(req.getContextPath().length());
	    
	    if ((path.contains("History") || path.contains("Account")) && loginBean != null && loginBean.getIsLoggedIn() == true) {
	    	chain.doFilter(request, response);
	    } 
	    else if((path.contains("History") || path.contains("Account")) && loginBean != null && loginBean.getIsLoggedIn() == false) {
	    	HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect("Login.xhtml");	    
	    } 
	    else {
	    	chain.doFilter(request, response);
	    }
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {}

}

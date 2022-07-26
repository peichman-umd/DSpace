package org.dspace.app.rest.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

// @Configuration
public class CASSecurityFilterChain implements SecurityFilterChain {
  private static final Logger log = LogManager.getLogger(CASLoginFilter.class);

  @Autowired
  private WebSecurityConfiguration webSecurityConfiguration;

  @Autowired
  private RestAuthenticationService restAuthenticationService;

  @Override
  public boolean matches(HttpServletRequest httpServletRequest) {
    log.info(httpServletRequest.getPathInfo());
    return httpServletRequest.getPathInfo().equals("/api/authn/cas");
  }

  @Override
  public List<Filter> getFilters() {
    AuthenticationManager authenticationManager;
    try {
      authenticationManager = webSecurityConfiguration.authenticationManagerBean();
    } catch (Exception e) {
      throw new RuntimeException("Unable to obtain AuthenticationManager");
    }
    List<Filter> filters = new ArrayList<>();
    filters.add(new CASLoginFilter("/api/authn/cas", authenticationManager, restAuthenticationService));
    return filters;
  }
}

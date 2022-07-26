package edu.umd.lib.dspace.app.rest.security;

import org.dspace.app.rest.security.CASLoginFilter;
import org.dspace.app.rest.security.RestAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@EnableWebSecurity
@Configuration
public class WebSecurityConfiguration extends org.dspace.app.rest.security.WebSecurityConfiguration {
  @Autowired
  private RestAuthenticationService restAuthenticationService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.addFilterBefore(
        new CASLoginFilter("/api/authn/cas", authenticationManager(), restAuthenticationService), LogoutFilter.class
    );
  }
}

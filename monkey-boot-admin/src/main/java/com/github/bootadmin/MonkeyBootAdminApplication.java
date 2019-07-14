package com.github.bootadmin;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;


import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;

/**
 * Spring Boot Admin 启动入口
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 * 
 * @date 2018-09-04
 * 
 */
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAdminServer
public class MonkeyBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(MonkeyBootAdminApplication.class, args);
	}
	
	@Configuration
    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {
		private final String adminContextPath;

	    public SecurityPermitAllConfig(AdminServerProperties adminServerProperties) {
	        this.adminContextPath = adminServerProperties.getContextPath();
	    }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
        	  SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
              successHandler.setTargetUrlParameter("redirectTo");
        	http.authorizeRequests()
            .antMatchers(adminContextPath + "/assets/**").permitAll()
            .antMatchers(adminContextPath + "/login").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and()
            .logout().logoutUrl(adminContextPath + "/logout").and()
            .httpBasic().and()
            .csrf().disable();
        }
    }
	
	@Bean
	public DingDingNotifier dingDingNotifier(InstanceRepository repository) {
		return new DingDingNotifier(repository);
	}
	
	@Primary
	@Bean(initMethod = "start", destroyMethod = "stop")
	public RemindingNotifier remindingNotifier(InstanceRepository repository) {
		RemindingNotifier notifier = new RemindingNotifier(dingDingNotifier(repository), repository);
		notifier.setReminderPeriod(Duration.ofSeconds(10));
		notifier.setCheckReminderInverval(Duration.ofSeconds(10));
		return notifier;
	}
}


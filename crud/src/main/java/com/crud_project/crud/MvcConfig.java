package com.crud_project.crud;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/home").setViewName("home");
		registry.addViewController("/").setViewName("home");
        registry.addViewController("/auth/login").setViewName("login");
        registry.addViewController("/auth/register").setViewName("register");
        registry.addViewController("/crud").setViewName("crud");
	}

}
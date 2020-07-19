package com.net128.oss.web.app.filemanager;

import com.net128.oss.web.lib.filemanager.FileManagerServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

@Configuration
public class AppConfiguration {
    private static final String mapping = "/file-manager/api";

    @Bean
    public ServletRegistrationBean<FileManagerServlet> fileManagerBean(MultipartConfigElement mce) {
        ServletRegistrationBean<FileManagerServlet> bean =
            new ServletRegistrationBean<>(new FileManagerServlet(), mapping);
        bean.setMultipartConfig(mce);
        bean.setLoadOnStartup(0);
        return bean;
    }
}

package com.hna.webserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Configuration for serving static Next.js files from /admin path.
 * In production, Next.js static export files are placed in resources/static/admin.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve Next.js static files from /admin
        // With basePath='/admin', files are in out/admin/ and copied to static/admin/
        // Handle /admin and /admin/** paths
        registry.addResourceHandler("/admin", "/admin/**")
                .addResourceLocations("classpath:/static/admin/")
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(true)
                .addResolver(new ResourceResolver() {
                    @Override
                    public Resource resolveResource(HttpServletRequest request, String requestPath,
                                                    List<? extends Resource> locations,
                                                    ResourceResolverChain chain) {
                        // If requesting /admin (without trailing slash), try to serve index.html
                        if (requestPath.equals("admin") || requestPath.equals("admin/")) {
                            return chain.resolveResource(request, "admin/index.html", locations);
                        }
                        return chain.resolveResource(request, requestPath, locations);
                    }

                    @Override
                    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                                 ResourceResolverChain chain) {
                        return chain.resolveUrlPath(resourcePath, locations);
                    }
                });
    }
}

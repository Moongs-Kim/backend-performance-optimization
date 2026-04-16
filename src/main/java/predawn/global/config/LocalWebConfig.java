package predawn.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import predawn.web.member.session.LoginCheckInterceptor;

@Configuration
public class LocalWebConfig implements WebMvcConfigurer {

    @Value("${local.file.dir}")
    private String fileDir;

    @Value("${local.file.view}")
    private String fileViewPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(fileViewPath + "/**")
                .addResourceLocations("file:///" + fileDir);

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/signup", "/api/signup/id-check", "/login", "/logout",
                        "/css/**", "/js/**", "/images/**", "/*.ico",
                        "/health", "/ip", "/error",
                        "/h2-console", "/api/dev/boards", "/dev/login-test"
                );
    }
}

package predawn.global.config;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import predawn.global.filter.RequestFilter;
import predawn.global.filter.limiter.RequestLimiter;
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
                        "/h2-console", "/api/dev/**", "/dev/login-test",
                        "/member/find-id", "/api/member/find-id",
                        "/api/password-reset/request", "/api/password-reset/confirm", "/member/password-reset"
                );
    }

    @Bean
    public FilterRegistrationBean<Filter> requestLimitFilter(RequestLimiter requestLimiter) {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new RequestFilter(requestLimiter));
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;
    }
}

package edu.unimag.pulsepass.persistence.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.unimag.pulsepass.persistence.exception.GlobalExceptionHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({
        SecurityConfig.class, 
        RestAuthenticationEntryPoint.class, 
        RestAccessDeniedHandler.class, 
        GlobalExceptionHandler.class
})
@MockitoBean(types = {JwtService.class, CustomUserDetailsService.class})
public @interface WebMvcSecurityTest {

}

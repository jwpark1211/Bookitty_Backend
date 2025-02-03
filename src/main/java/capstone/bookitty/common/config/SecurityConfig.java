package capstone.bookitty.common.config;

import capstone.bookitty.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers((headersConfig) ->
                        headersConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers(antMatcher("/"),
                                        antMatcher("/open/**"),
                                        antMatcher("/members/test"),
                                        antMatcher("/members/login"),
                                        antMatcher("/members/reissue"),
                                        antMatcher("/members/new"),
                                        antMatcher("/members/email/**"),
                                        antMatcher("/swagger-ui/**"),
                                        antMatcher("/star/all"),
                                        antMatcher("/star/isbn/**"),
                                        antMatcher("/comment/all"),
                                        antMatcher("/state/all"),
                                        antMatcher("/comment/isbn/**"),
                                        antMatcher("/v3/**")).permitAll()
                                .requestMatchers(antMatcher("/star/new")).authenticated()
                                .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout((logout) ->
                        logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .invalidateHttpSession(true)
                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                                .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(new JwtFilter(jwtTokenProvider, jwtProperties), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

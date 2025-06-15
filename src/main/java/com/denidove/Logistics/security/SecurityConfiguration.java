package com.denidove.Logistics.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private CustomWebAuthenticationDetailsSource authenticationDetailsSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        //toDo сделать нормальную шифровку паролей
        //return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }


    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }


    // Один из вариантов подключения кастомного AuthenticationProvider.
    // Мы его встраиваем в конфигурацию AuthenticationManager

    /*
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }
    */

    @Bean
    public DaoAuthenticationProvider authProvider() {
        CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // Depricated
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //http.httpBasic(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);
        //http.userDetailsService(userDetailsService()); // Не работает для установки кастомного userDetailsService
        //http.authenticationProvider(authProvider()); // Spring автоматически подхватывает этот бин authProvider()
        http.formLogin(
                form -> form
                        .authenticationDetailsSource(authenticationDetailsSource)
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        //.defaultSuccessUrl("/products", true)
                        .successHandler((req, resp, auth) -> { // * действия в случае успешной авторизации
                            //userSessionService.persistProductInCart(); // записываем добавленные товары неавторизованного пользователя
                            resp.setStatus(HttpStatus.OK.value());
                            resp.sendRedirect("/"); // переход на главную страницу
                        }).permitAll());

        //toDo сделать нормальные инициалы в шаблоне orderok.html

        http.authorizeHttpRequests(
                requests -> {
                    //              указываем паттерн "/**" т.к у нас в уже установлено "spring.thymeleaf.prefix=classpath:/static/"
                    requests.requestMatchers("/js/**", "/styles/**", "/images/**", "/",
                                    "/registration", "/adduser", "/verify").permitAll();
                    requests.requestMatchers(HttpMethod.POST,"/order-dto").permitAll();
                    requests.requestMatchers(HttpMethod.GET, "/admin-lk", "/active-orders").hasAuthority("Admin");
                    requests.anyRequest().authenticated();
                }
        );

        http.logout(e -> e.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").deleteCookies("JSESSIONID")
                .invalidateHttpSession(true));

        return http.build();

    }
}

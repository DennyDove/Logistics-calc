package com.denidove.Logistics.security;

import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.security.jwt.JwtAuthenticationFilter;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // данная аннотация создает конструктор полей private final
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final GuestContextFilter guestContextFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserSessionService userSessionService;
    private final CustomWebAuthenticationDetailsSource authenticationDetailsSource;
    private final AuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationEntryPoint customEntryPoint;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //Обрабатывает как первый шаг (логин/пароль), так и второй (код).
    // Здесь вручную добавляем бин, т.к. создаём CustomAuthenticationProvider с параметрами конструктора.
    // Если просто добавить @Component на сам класс, Spring не сможет корректно его собрать автоматически и может получиться дублирующая регистрация.
    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(userRepository, userRedisService, passwordEncoder());
    }

    //✅ AuthenticationManager, который использует наш CustomAuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(CustomAuthenticationProvider provider) {
        return new ProviderManager(List.of(provider));
    }


    //✅ Обработчик ошибок аутентификации (редиректит с кодами)
    /* Убираем явное создание бина из конфигурации, т.к. класс AuthenticationFailureLogic уже помечен аннотацией @Component
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureLogic();
    }
    */

    /* Для отладки корректного порта (чтобы побороть редирект на 8443)
    @Bean
    public PortMapper portMapper() {
        PortMapperImpl mapper = new PortMapperImpl();
        Map<String, String> map = new HashMap<>();
        map.put("8080", "8080"); // 👈 http->https (теперь HTTPS=8080)
        mapper.setPortMappings(map);
        return mapper;
    }*/


    private static final String[] PUBLIC_MATCHERS = {
            "/", "/js/**", "/styles/**", "/images/**",
            "/registration", "/verify", "/verify2", "/confirm",
            "/login", "/login-1", "/login-2", "/reset-page", "/reset"
    };

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/js/**", "/styles/**", "/images/**", "/favicon.ico");
    }

    /**
     * ✅ Основная конфигурация безопасности
     */


    //Мы добавили WebSecurityCustomizer, поэтому первую цепочку staticResourcesChain можно полностью удалить.
    /*
    @Bean
    @Order(1)
    public SecurityFilterChain staticResourcesChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/styles/**", "/images/**", "/js/**", "/favicon.ico")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .requestCache(cache -> cache.disable())
                .securityContext(ctx -> ctx.disable())
                .sessionManagement(sess -> sess.disable())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
    */

    @Bean
    @Order(1)
    public SecurityFilterChain securityFormLoginChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/login-1", "/login-2", "/verify-code", "/registration", "/adduser", "/confirm", "/calc-dto", "/vozcalc", "/delline", "/reset-page", "/reset-mail", "/reset", "/save-pass")
                /* Для отладки корректного порта (чтобы побороть редирект на 8443)
                .portMapper(pm -> pm
                       .http(8080).mapsTo(8080) // 👈 говорим, что HTTPS = 8080
                )*/
                .csrf(csrf -> csrf.disable()) // 🚫 Отключаем CSRF, т.к. у нас формы POST-запросов генерируются Thymeleaf
                .cors(cors -> cors.disable()) // 🚫 Отключаем CORS, т.к. фронт и бэк работают на одном origin
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                /* Задел на будущее, если фронтэнд будет запущен на другом порту (например: http://localhost:3030)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:8080")); // фронт и бэк на одном порту
                    config.setAllowedMethods(List.of("GET", "POST"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))*/
                .formLogin(form -> form
                        .authenticationDetailsSource(authenticationDetailsSource)
                        .loginPage("/login-1")                      // форма логина (первый шаг)
                        .loginProcessingUrl("/login")               // точка входа первого шага
                        .failureHandler(authFailureHandler)
                        .successHandler((req, resp, auth) -> {
                            // первый шаг успешен — уходим на /login-2 (ввод кода)
                            resp.setStatus(HttpStatus.OK.value());
                            resp.sendRedirect("/login-2");
                        })
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/calc-dto", "/vozcalc", "/delline", "/login", "/adduser", "/verify-code", "/reset-mail", "/save-pass").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityJwtChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().none()  // для STATELESS это более надежно
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(HttpMethod.POST, /*"/calc-dto", "/vozcalc", "/delline",*/ "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin-lk", "/active-orders").hasAuthority("Admin")
                        .anyRequest().authenticated()
                )
                //.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // Закомментировал этот вариант, т.к. при этом при запросе на /order аутентификация слетала (самопроизвольно устанавливался anonymous-token)
                .addFilterBefore(jwtAuthFilter, AnonymousAuthenticationFilter.class) // Это гарантирует, что JWT-фильтр выполнится раньше, чем Spring выставит anonymous-token, и корректно заполнит SecurityContextHolder для текущего запроса.
                .addFilterAfter(guestContextFilter, JwtAuthenticationFilter.class) // фильтр для сохранения состояния неавторизованного пользователя
                .logout(logout -> logout
                        .logoutUrl("/logout") // эндпоинт выхода
                        .addLogoutHandler((request, response, authentication) -> {
                            // Удаляем cookie с JWT
                            ResponseCookie cookie = ResponseCookie.from("jwt", "")
                                    .httpOnly(true)
                                    .secure(true)
                                    .path("/")
                                    .maxAge(0)
                                    .sameSite("Strict")
                                    .build();
                            response.addHeader("Set-Cookie", cookie.toString());
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.sendRedirect("/login-1?logout=true");
                        })
                        .permitAll()
                );

        return http.build();
    }
}
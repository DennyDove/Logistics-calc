package com.denidove.Logistics.security;

import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.security.jwt.JwtAuthenticationFilter;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.security.phone.PhoneAuthenticationFilter;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // данная аннотация создает конструктор полей private final
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final GuestContextFilter guestContextFilter;
    private final UserSessionService userSessionService;
    private final JwtService jwtService;
    //private final AuthenticationFailureHandler authFailureHandler; // обработка данной логики должна быть вынесена в фильтры, и не должна привязываться в SecurityConfig
    private final CustomAuthenticationEntryPoint customEntryPoint;
    private final FixedLoginRedirectEntryPoint fixedLoginRedirectEntryPoint;

    private final PasswordEncoder passwordEncoder; // теперь приходит извне
    private final JwtAuthenticationFilter jwtAuthFilter;
    //private final SmsSendFilter smsSendFilter;
    private final CustomAuthenticationProvider loginPasswordProvider;
    /** Убираем это поле иначе появляется циклическая зависимость
    private final PhoneAuthenticationFilter phoneAuthFilter;
    */
    private final PhoneAuthenticationFilter phoneAuthenticationFilter;

    /** Нельзя держать PasswordEncoder в SecurityConfiguration.
     * Нужно вынести encoder в отдельный infrastructure-config и использовать его как обычный бин
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */



    /** Удаляем этот бин, т.к. образуется циклическая зависимость

    //Обрабатывает как первый шаг (логин/пароль), так и второй (код).
    // Здесь вручную добавляем бин, т.к. создаём CustomAuthenticationProvider с параметрами конструктора.
    // Если просто добавить @Component на сам класс, Spring не сможет корректно его собрать автоматически и может получиться дублирующая регистрация.
    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(userRepository, userRedisService, passwordEncoder);
    }
    */

    @Bean
    public PortMapper portMapper() {
        PortMapperImpl mapper = new PortMapperImpl();
        mapper.setPortMappings(Map.of("8080", "8444"));
        return mapper;
    }


    private static final String[] PUBLIC_MATCHERS = {
            "/", "/js/**", "/styles/**", "/images/**",
            "/registration", "/auth/verify", "/verify2", "/confirm", "/login-main",
            "/auth/login-1", "/auth/login-2", "/auth/request-sms", "/reset-page", "/reset"
    };

    private static final String[] PUBLIC_MATCHERS_POST = {
            "/api/calc-dto", "/api/vozcalc", "/api/delline",

            "/auth/login", "/api/auth/phone-send-code",
            "/auth/phone-login",
            "/auth/verify-code",
            "/api/auth/adduser",
            "/reset-mail", "/save-pass"
    };

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/js/**", "/styles/**", "/images/**", "/favicon.ico");
    }

    /**  Нужно создавать фильтр ручным вызовом new, а не через @Bean - иначе будет циклическая зависимость
    @Bean
    public PhoneAuthenticationFilter phoneAuthFilter(AuthenticationManager am) {
        return new PhoneAuthenticationFilter(am);
    }
    */

    // ============================================================
    // AuthenticationManager Bean - этот создаёт цикл. Хотя это официальный "spring-way".
    // ============================================================
    /**
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    */


    /**
     * ✅ Основная конфигурация безопасности
     */



    // ============================================================
    // 🟢 CHAIN 1 — PHONE/SMS LOGIN (PRIMARY LOGIN)
    // ============================================================

    @Bean
    @Order(1)
    public SecurityFilterChain phoneApiAuthChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/auth/**")
                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .requestCache(rc -> rc.disable())
                //toDo разобраться!
                /* Убираем этот блок, т.к. это уже прописано в PHONE FAILURE HANDLER
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> {
                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            /* Это выдается на фронте
                            resp.setContentType("application/json");
                            resp.getWriter().write("""
                                { "error": "UNAUTHORIZED" }
                            """);
                        }
                )*/
                .addFilterBefore(phoneAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                )
                //toDo Разобраться что делает этот блок! - Разобрался. Комментарии см. ниже.
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                );

        return http.build();
    }

    /**
     * auth.anyRequest().permitAll() -- Т.к. в первой цепочке безопасность обеспечивается:
    - валидацией входных данных
    - проверкой SMS/кода
    - логикой AuthenticationProvider
    - созданием Authentication после успешной проверки
    А не через authorizeHttpRequests.

    */

    // ============================================================
    // 🟡 CHAIN 2 — LOGIN/PASSWORD + 2FA (ALTERNATIVE LOGIN)
    // ============================================================

    @Bean
    @Order(2)
    public SecurityFilterChain loginPasswordChain(HttpSecurity http) throws Exception {

        //toDo спросить у GPT правильное ли это рещение? Спросил - неправильное!
        //AuthenticationManager authenticationManager = new ProviderManager(List.of(loginPasswordProvider));

        http
                .securityMatcher("/auth/login-1", "/auth/login", "/auth/login-2", "/auth/verify-code")
                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(loginPasswordProvider)
                .formLogin(form -> form
                        .loginPage("/auth/login-1")
                        .loginProcessingUrl("/auth/login")
                        .successHandler((req, resp, auth) -> {
                            resp.sendRedirect("/auth/login-2");
                        })
                        //toDo сделать нормальный переход на нормальную страницу login-1
                        .failureUrl("/auth/login-1?error")
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                                .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }

    // ============================================================
    // 🟦 CHAIN 3 — JWT CHAIN (MAIN)
    // ============================================================

    @Bean
    @Order(3)
    public SecurityFilterChain jwtChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")

                /*.securityMatcher(new AndRequestMatcher(
                        new AntPathRequestMatcher("/api/**"),
                        new NegatedRequestMatcher(new AntPathRequestMatcher("/api/auth/**"))
                ))*/


                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                        // остальные требуют auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(guestContextFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // 🔴 CHAIN 4 — UI-CHAIN REDIRECT TO LOGIN-MAIN
    // ============================================================

    // Есть ли целосообразность этой четвертой цепочки?

    //toDo четко определить SecurityMatcher для 4-х цепочек!

    @Bean
    @Order(4)
    SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                //toDo Разобраться пишем ли состояние SessionCreationPolicy -> IF_REQUIRED ?
                //toDo И вообще у меня в Cookies - одновременно и JSESSIONID и jwt-токен. Так наверное не должно быть...

        /** Не хочет заходить в /user-lk, опять выстреливает порт 8443:
        2025-12-30T17:39:09.523+03:00 DEBUG 1624 --- [Logistics] [nio-8080-exec-1] o.s.security.web.FilterChainProxy        : Securing GET /user-lk
        2025-12-30T17:39:09.526+03:00 DEBUG 1624 --- [Logistics] [nio-8080-exec-1] o.s.s.w.a.AnonymousAuthenticationFilter  : Set SecurityContextHolder to anonymous SecurityContext
        2025-12-30T17:39:09.527+03:00 DEBUG 1624 --- [Logistics] [nio-8080-exec-1] o.s.s.w.s.HttpSessionRequestCache        : Saved request https://localhost:8443/user-lk?continue to session
        2025-12-30T17:39:09.528+03:00 DEBUG 1624 --- [Logistics] [nio-8080-exec-1] o.s.s.web.DefaultRedirectStrategy        : Redirecting to https://localhost:8443/login-main

        Разобрался! Чтобы устранить эту ошибку, нужно было добавить:
         .addFilterBefore(jwtAuthFilter, AnonymousAuthenticationFilter.class)

         Иначе данный запрос на /user-lk считался неаутентифицированным (т.к не попадал в jwt-цепочку)

        */

                .securityMatcher("/**") //   /login-main обрабатывается в этой цепочке
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(fixedLoginRedirectEntryPoint)
                )

                /** Вариант, при котором выскакивает порт 8443. Чтобы это исправить нужно жестко прописать порт 8080.
                 *  см. код выше.
                 *
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login-main")
                        )
                )*/
                //toDo Разобраться с кодом ниже:
                .addFilterBefore(jwtAuthFilter, AnonymousAuthenticationFilter.class)
                .anonymous(Customizer.withDefaults())
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
                    response.sendRedirect("/login-main");
                })
                .permitAll()
        );

        return http.build();
    }

}
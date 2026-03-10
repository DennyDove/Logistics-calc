/*
package com.denidove.Logistics.security;

import com.denidove.Logistics.entities.SecurityUser;
import com.denidove.Logistics.repositories.UserRepository;
import com.denidove.Logistics.security.jwt.JwtAuthenticationFilter;
import com.denidove.Logistics.security.jwt.JwtService;
import com.denidove.Logistics.security.phone.PhoneAuthenticationFilter;
import com.denidove.Logistics.security.phone.SmsSendFilter;
import com.denidove.Logistics.services.UserRedisService;
import com.denidove.Logistics.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
*/

/**
 * Старая версия конфигурации с SendSmsFilter - данный фильтр сам отправлял сообщения (бизнес-логика), при этом у нас был пустой контроллер.
 * В итоге, решили, что такая архитектура слишком неявная и неочевидная, хотя допустимая.
 * Более очевидное решение, чтобы бизнес логика всё-таки была в контроллере.
 */

/*
//@Configuration
//@EnableWebSecurity
@RequiredArgsConstructor // данная аннотация создает конструктор полей private final
public class SecurityConfiguration_old {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final GuestContextFilter guestContextFilter;
    private final UserSessionService userSessionService;
    private final JwtService jwtService;
    private final AuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationEntryPoint customEntryPoint;

    private final PasswordEncoder passwordEncoder; // теперь приходит извне
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final SmsSendFilter smsSendFilter;
    private final PhoneAuthenticationProvider phoneAuthProvider;
    private final CustomAuthenticationProvider loginPasswordProvider;
    /** Убираем это поле иначе появляется циклическая зависимость
    private final PhoneAuthenticationFilter phoneAuthFilter;
    */


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


/*
    private static final String[] PUBLIC_MATCHERS = {
            "/", "/js/**", "/styles/**", "/images/**",
            "/registration", "/verify", "/verify2", "/confirm", "/login-main",
            "/login-1", "/login-2", "/reset-page", "/reset", "/request-sms"
    };

    private static final String[] PUBLIC_MATCHERS_POST = {
            "/api/calc-dto", "/api/vozcalc", "/api/delline",

            "/api/auth/login", "/api/auth/phone-send-code",
            "/api/auth/phone-login",
            "/api/auth/verify-code", "/api/auth/reset-mail",
            "/api/auth/save-pass", "/api/auth/adduser"

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

/*
    // ============================================================
    // 🟦 CHAIN 1 — JWT CHAIN (MAIN)
    // ============================================================

    @Bean
    @Order(1)
    public SecurityFilterChain jwtChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")
                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // разрешаем страницы входа
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                        // остальные требуют auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // 🟢 CHAIN 2 — PHONE/SMS LOGIN (PRIMARY LOGIN)
    // ============================================================

    @Bean
    @Order(2)
    public SecurityFilterChain phoneLoginChain(HttpSecurity http) throws Exception {

        AuthenticationManager phoneAuthManager =
                new ProviderManager(List.of(phoneAuthProvider));

        http
                .securityMatcher("/login-main", "/api/auth/phone-login", "api/auth/phone-send-code", "api/auth/phone-verify")
                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(phoneAuthProvider)
                .formLogin(form -> form
                        .loginPage("/login-main")
                        .loginProcessingUrl("/phone-login")
                        .successHandler((req, resp, auth) -> {
                            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
                            Long userId = securityUser.getId();
                            String jwt = jwtService.generateToken(userId);
                            jwtService.addJwtCookie(resp, jwt);
                            resp.sendRedirect("/");
                        })
                        .failureUrl("/login-main?error")
                )
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Passwordless pipeline
                .addFilterBefore(smsSendFilter, UsernamePasswordAuthenticationFilter.class)
                //              new - для устранения циклической зависимости
                .addFilterAfter(new PhoneAuthenticationFilter(phoneAuthManager), SmsSendFilter.class);

        return http.build();
    }

    // ============================================================
    // 🟡 CHAIN 3 — LOGIN/PASSWORD + 2FA (ALTERNATIVE LOGIN)
    // ============================================================

    @Bean
    @Order(3)
    public SecurityFilterChain loginPasswordChain(HttpSecurity http) throws Exception {

        //toDo спросить у GPT правильное ли это рещение:
        //AuthenticationManager authenticationManager = new ProviderManager(List.of(loginPasswordProvider));

        http
                .securityMatcher("/login-1", "/login", "/login-2", "/verify-code")
                .csrf(cs -> cs.disable())
                .cors(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(loginPasswordProvider)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((req, resp, auth) -> {
                            resp.sendRedirect("/login-2");
                        })
                        .failureUrl("/login-1?error")
                )
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                                .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }

    // ============================================================
    // 🔴 CHAIN 4 — UI-CHAIN REDIRECT TO LOGIN-MAIN
    // ============================================================

    // больше даннв 4-а цепочка не нужна


}
*/

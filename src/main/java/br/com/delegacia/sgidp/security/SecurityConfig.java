package br.com.delegacia.sgidp.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${CORS_ORIGENS_PERMITIDAS:http://localhost:4200}")
    private String origensPermitidas;

    // Bloco 1 - senha: hash BCrypt (já existia)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bloco 2 - as regras do "segurança da porta"
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        // Rotas Públicas: login, solicitação de cadastro e papéis do formulário de cadastro
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/usuarios/cadastro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/roles/cadastro").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Lista para os seletores (destinatário, responsável, detentor): vem antes da regra geral
                        .requestMatchers(HttpMethod.GET, "/usuarios/operacionais").hasAnyRole("DELEGADO", "POLICIAL", "ESTAGIARIO")
                        // Gestão de usuários: só Admin
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        // gestão de procedimentos
                        .requestMatchers(HttpMethod.POST, "/procedimentos").hasAnyRole("DELEGADO", "POLICIAL")
                        // Exportação de Procedimento: Só Delegado e Policial
                        .requestMatchers(HttpMethod.GET, "/procedimentos/exportar").hasAnyRole("DELEGADO", "POLICIAL")
                        // Repasse de custódia: Delegado, Policial e Estagiário (detentor atual)
                        .requestMatchers(HttpMethod.POST, "/repasses").hasAnyRole("DELEGADO", "POLICIAL", "ESTAGIARIO")
                        // Exportação de veículos (relatório de carga): só Delegado/Policial. Antes da regra de GET
                        // abaixo, porque o Spring usa a primeira regra que combina com a rota
                        .requestMatchers(HttpMethod.GET, "/veiculos/exportar").hasAnyRole("DELEGADO", "POLICIAL")
                        // Veículos: consulta para qualquer logado (inclusive Estagiário); escrita só Delegado/Policial
                        .requestMatchers(HttpMethod.GET, "/veiculos/**").authenticated()
                        .requestMatchers("/veiculos/**").hasAnyRole("DELEGADO", "POLICIAL")
                        // Oitivas: Delegado, Policial e Estagiário
                        .requestMatchers("/oitivas/**").hasAnyRole("DELEGADO", "POLICIAL", "ESTAGIARIO")
                        // Pessoas: Delegado, Policial e Estagiário (os mesmos das oitivas)
                        .requestMatchers("/pessoas/**").hasAnyRole("DELEGADO", "POLICIAL", "ESTAGIARIO")
                        // Painel e Minha mesa: Delegado, Policial e Estagiário (cada um vê os próprios dados)
                        .requestMatchers(HttpMethod.GET, "/painel/**").hasAnyRole("DELEGADO", "POLICIAL", "ESTAGIARIO")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePapeis())));
        return http.build();
    }

    // Bloco 3 - a chave que assina e confere o token (vem do .env)
    private SecretKey chaveSecreta() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveSecreta()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(chaveSecreta())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Bloco 4 - ensina o Spring a ler o papel de dentro do token
    private JwtAuthenticationConverter conversorDePapeis() {
        JwtGrantedAuthoritiesConverter papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName("role");   // o papel está no campo "role" do token
        papeis.setAuthorityPrefix("");            // já vem "ROLE_ADMIN"; sem isso viraria "SCOPE_ROLE_ADMIN"

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis);
        return conversor;
    }

    // Bloco 5 - CORS: quais sites (origens) podem chamar a API pelo navegador

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origensPermitidas.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Content-Disposition")); // Deixa o front ler o nome do arquivo baixado (exportação); sem isso o navegador esconde o cabeçalho
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
package br.com.delegacia.sgidp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.delegacia.sgidp.model.user.Usuario;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor //Lombok gera o construtor que seria feito à mão
public class TokenService {

    private static final long VALIDADE_HORAS = 8;   // um turno de trabalho

    private final JwtEncoder jwtEncoder;

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet dados = JwtClaimsSet.builder()
                .issuer("sgi-dp")                                     // quem emitiu
                .issuedAt(agora)                                      // quando
                .expiresAt(agora.plus(VALIDADE_HORAS, ChronoUnit.HOURS))
                .subject(usuario.getLogin())                          // quem é
                .claim("nome", usuario.getNome())                     // para o Angular mostrar
                .claim("role", usuario.getRole().getAcesso())         // o cargo: ROLE_...
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, dados)).getTokenValue();
    }
}
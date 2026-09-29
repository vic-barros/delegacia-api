package br.com.delegacia.sgidp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponseDto> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(CadastroNaoAprovadoException.class)
    public ResponseEntity<ErroResponseDto> cadastroNaoAprovado(CadastroNaoAprovadoException e) {
        return resposta(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // Erros de Bean Validation nos DTOs (@NotBlank etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDto> dadosInvalidos(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErroResponseDto(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", detalhes));
    }

    private ResponseEntity<ErroResponseDto> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponseDto(status.value(), mensagem));
    }
}

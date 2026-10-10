package br.com.delegacia.sgidp.exception;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

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

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDto> regraViolada(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDto> recursoNaoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErroResponseDto> recursoDuplicado(RecursoDuplicadoException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    // Exceção para parâmetro com tipo errado (Ex: GET /usuarios?status=XYZ, XYZ não é enum de status
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDto> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'");
    }

    // Exceção para JSON ilegível ou mal formatado, como uma vírgula faltando
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDto> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado");
    }

    // Exceção para Erro 409 da Confirmação Necessária no Front
    @ExceptionHandler(ConfirmacaoNecessariaException.class)
    public ResponseEntity<ErroResponseDto>confirmacaoNecessaria(ConfirmacaoNecessariaException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponseDto(HttpStatus.CONFLICT.value(),
                e.getMessage(), List.of(e.getCampoConfirmacao())));
    }
}

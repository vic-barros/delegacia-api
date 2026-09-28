package br.com.delegacia.sgidp.exception;

// Mensagem genérica de propósito: não revela se o erro foi no login ou na senha (UC01)
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos");
    }
}

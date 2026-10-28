package br.com.delegacia.sgidp.exception;

// Usuário autenticado e com o papel certo, mas sem permissão sobre este registro
// (ex.: não é o detentor do procedimento nem Delegado; não é o destinatário do repasse) → 403
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String message) {

        super(message);
    }
}

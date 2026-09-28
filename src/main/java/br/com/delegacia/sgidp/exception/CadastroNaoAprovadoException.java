package br.com.delegacia.sgidp.exception;

public class CadastroNaoAprovadoException extends RuntimeException {

    public CadastroNaoAprovadoException(String mensagem) {
        super(mensagem);
    }
}

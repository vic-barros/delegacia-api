package br.com.delegacia.sgidp.exception;

import lombok.Getter;

//O front mostra o aviso e, se ele confirmar, reenvia a mesma requisição com a flag (campoConfirmacao)= true
@Getter
public class ConfirmacaoNecessariaException extends RuntimeException {

    private final String campoConfirmacao;

    public ConfirmacaoNecessariaException(String message, String campoConfirmacao) {

        super(message);
        this.campoConfirmacao = campoConfirmacao;
    }
}

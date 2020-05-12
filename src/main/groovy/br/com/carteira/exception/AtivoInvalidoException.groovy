package br.com.carteira.exception

class AtivoInvalidoException extends RuntimeException{
    AtivoInvalidoException(String mensagem) {
        super(mensagem)
    }
}

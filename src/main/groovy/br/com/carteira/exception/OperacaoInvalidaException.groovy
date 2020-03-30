package br.com.carteira.exception

class OperacaoInvalidaException extends RuntimeException{
    OperacaoInvalidaException(String mensagem) {
        super(mensagem)
    }
}

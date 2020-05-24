package br.com.carteira.exception

class ConsultaInvalidaException extends RuntimeException{
    ConsultaInvalidaException(String mensagem) {
        super(mensagem)
    }
}

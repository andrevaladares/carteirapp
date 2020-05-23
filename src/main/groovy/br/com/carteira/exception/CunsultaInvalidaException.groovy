package br.com.carteira.exception

class CunsultaInvalidaException extends RuntimeException{
    CunsultaInvalidaException(String mensagem) {
        super(mensagem)
    }
}

package br.com.carteira.exception

class ArquivoInvalidoException extends RuntimeException{
    ArquivoInvalidoException(String mensagem) {
        super(mensagem)
    }
}

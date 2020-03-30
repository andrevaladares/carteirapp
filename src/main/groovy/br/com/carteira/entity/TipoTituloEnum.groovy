package br.com.carteira.entity

enum TipoTituloEnum {
    a('ações'),
    f('fundos imobiliários'),
    i('fundos de índice')

    private final String nomeTitulo

    TipoTituloEnum(String nomeTitulo) {
        this.nomeTitulo = nomeTitulo
    }

    String getNomeTitulo() {
        return nomeTitulo
    }

}
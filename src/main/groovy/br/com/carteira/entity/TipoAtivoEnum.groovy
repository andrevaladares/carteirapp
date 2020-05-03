package br.com.carteira.entity

enum TipoAtivoEnum {
    a('ações'),
    f('fundos imobiliários'),
    i('fundos de índice')

    private final String nomeAtivo

    TipoAtivoEnum(String nomeAtivo) {
        this.nomeAtivo = nomeAtivo
    }

    String getNomeAtivo() {
        return nomeAtivo
    }

}
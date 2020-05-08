package br.com.carteira.entity

enum TipoAtivoEnum {
    a('ações', 'br.com.carteira.entity.OperacoesAtivoComum'),
    f('fundos imobiliários', 'br.com.carteira.entity.OperacoesAtivoComum'),
    i('fundos de índice', 'br.com.carteira.entity.OperacoesAtivoComum'),
    o('ouro oz2', 'br.com.carteira.entity.OperacoesOuroOz2')

    private final String nomeAtivo
    private final String classeOperacao

    TipoAtivoEnum(String nomeAtivo, String classeOperacao) {
        this.nomeAtivo = nomeAtivo
        this.classeOperacao = classeOperacao
    }

    String getNomeAtivo() {
        return nomeAtivo
    }

    String getClasseOperacao() {
        return classeOperacao
    }

}
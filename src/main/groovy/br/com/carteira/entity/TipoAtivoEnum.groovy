package br.com.carteira.entity

enum TipoAtivoEnum {
    a('ações', 'br.com.carteira.entity.OperacoesAtivoComum'),
    fii('fundos imobiliários', 'br.com.carteira.entity.OperacoesAtivoComum'),
    fin('fundos de índice', 'br.com.carteira.entity.OperacoesAtivoComum'),
    oz2('ouro oz2', 'br.com.carteira.entity.OperacoesOuroOz2'),
    fiv('fundos de investimento', 'br.com.carteira.entity.OperacoesFundoInvestimento'),
    tis('tesouro ipca juros semestrais', 'br.com.carteira.entity.OperacoesFundoInvestimento')

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
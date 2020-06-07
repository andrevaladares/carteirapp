package br.com.carteira.entity

enum TipoAtivoEnum {
    a('ações', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker'),
    fii('fundos imobiliários', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker'),
    fin('fundos de índice', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker'),
    oz2('ouro oz2', 'br.com.carteira.entity.OperacoesOuroOz2', 'ticker'),
    fiv('fundos de investimento', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'cnpj_fundo'),
    tis('tesouro ipca juros semestrais', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome'),
    tse('tesouro selic', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome'),
    tps('tesouro prefixado com juros semestrais', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome'),
    deb('debenture', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome'),
    cri('cri', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome'),
    cdb('cdb', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome')

    private final String nomeAtivo
    private final String classeOperacao
    private final String idNoBanco

    TipoAtivoEnum(String nomeAtivo, String classeOperacao, String idNoBanco) {
        this.nomeAtivo = nomeAtivo
        this.classeOperacao = classeOperacao
        this.idNoBanco = idNoBanco
    }

    String getNomeAtivo() {
        nomeAtivo
    }

    String getClasseOperacao() {
        classeOperacao
    }

    String getIdNoBanco() {
        idNoBanco
    }

}
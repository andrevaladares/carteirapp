package br.com.carteira.entity

enum TipoAtivoEnum {
    a('ações', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker', 'ticker'),
    aus('ações estados unidos', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker', 'ticker'),
    m('moeda', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker', 'ticker'),
    fii('fundos imobiliários', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker', 'ticker'),
    fin('fundos de índice', 'br.com.carteira.entity.OperacoesAtivoComum', 'ticker', 'ticker'),
    oz2('ouro oz2', 'br.com.carteira.entity.OperacoesOuroOz2', 'ticker', 'ticker'),
    fiv('fundos de investimento', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'cnpj_fundo', 'nome'),
    tis('tesouro ipca juros semestrais', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome', 'nome'),
    tse('tesouro selic', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome', 'nome'),
    tps('tesouro prefixado com juros semestrais', 'br.com.carteira.entity.OperacoesFundoInvestimento', 'nome', 'nome'),
    deb('debenture', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome', 'nome'),
    cri('cri', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome', 'nome'),
    cdb('cdb', 'br.com.carteira.entity.OperacoesAtivoComum', 'nome', 'nome')

    private final String nomeAtivo
    private final String classeOperacao
    private final String idNoBanco
    private final String idEmSituacaoCarteira

    TipoAtivoEnum(String nomeAtivo, String classeOperacao, String idNoBanco, String idEmSituacaoCarteira) {
        this.nomeAtivo = nomeAtivo
        this.classeOperacao = classeOperacao
        this.idNoBanco = idNoBanco
        this.idEmSituacaoCarteira = idEmSituacaoCarteira
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

    String getIdEmSituacaoCarteira() {
        return idEmSituacaoCarteira
    }

    static List<TipoAtivoEnum> getDebCriTesouroFundo(){
        [deb, cri] + getTesouroFundo()
    }

    static List<TipoAtivoEnum> getTesouroFundo(){
        [tis,tse,tps,fiv]
    }

}
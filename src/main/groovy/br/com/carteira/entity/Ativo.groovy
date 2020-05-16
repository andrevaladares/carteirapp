package br.com.carteira.entity

import java.math.RoundingMode
import java.time.LocalDate

class Ativo {
    Long id
    String ticker
    String nome
    TipoAtivoEnum tipo
    String setor
    BigDecimal qtde
    BigDecimal valorTotalInvestido
    LocalDate dataEntrada
    String cnpjFundo
    OperacoesAtivo operacoesAtivo

    private Ativo() {
    }

    BigDecimal obterCustoMedioUnitario() {
        operacoesAtivo.obterCustoMedioUnitario(valorTotalInvestido, qtde)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, BigDecimal qtde) {
        operacoesAtivo.obterResultadoVenda(custoMedioVenda, valorTotalOperacao, qtde)
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Ativo atualizarAtivoAPartirDaOperacao(Operacao operacao) {
        def retornoAtualizacao
        if (qtde < 0 || (qtde == 0 && operacao.tipoOperacao == TipoOperacaoEnum.v)) {
            retornoAtualizacao = operacoesAtivo.atualizarAtivoOperacaoShort(operacao, this.qtde, this.valorTotalInvestido)
        } else {
            retornoAtualizacao = atualizarAtivoOperacaoComum(operacao)
        }
        this.qtde = retornoAtualizacao.qtde
        this.valorTotalInvestido = retornoAtualizacao.valorTotalInvestido

        this
    }

    Ativo atualizarAtivoOperacaoComum(Operacao operacao) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            def valorInvestidoEquivalente = (valorTotalInvestido.divide(qtde, 8, RoundingMode.HALF_UP) * operacao.qtde).setScale(8, RoundingMode.HALF_UP)
            qtde -= operacao.qtde
            valorTotalInvestido -= valorInvestidoEquivalente

        } else {
            qtde += operacao.qtde
            valorTotalInvestido += operacao.valorTotalOperacao
        }

        return this
    }

    static Ativo getInstance() {
        new Ativo(operacoesAtivo: new OperacoesAtivoComum())
    }

    static Ativo getInstanceWithAtributeMap(Map atributes) {
        def ativo = new Ativo(atributes)
        if(ativo.tipo == null)
            ativo.tipo = TipoAtivoEnum.a
        ativo.operacoesAtivo = Class.forName(ativo.tipo.classeOperacao).newInstance()

        ativo
    }

    void validar() {
        operacoesAtivo.validar(this)
    }

    GString obterQueryUpdate() {
        operacoesAtivo.obterQueryUpdate(this)
    }
}
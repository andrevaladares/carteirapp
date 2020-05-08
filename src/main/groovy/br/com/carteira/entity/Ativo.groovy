package br.com.carteira.entity

import java.math.RoundingMode
import java.time.LocalDate

class Ativo {
    Long id
    String ticker
    String nome
    TipoAtivoEnum tipo
    String setor
    Integer qtde
    BigDecimal valorTotalInvestido
    LocalDate dataEntrada
    OperacoesAtivo operacoesAtivo

    private Ativo() {
    }

    BigDecimal obterCustoMedio() {
        operacoesAtivo.obterCustoMedio(valorTotalInvestido, qtde)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, Integer qtde) {
        operacoesAtivo.obterResultadoVenda(custoMedioVenda, valorTotalOperacao, qtde)
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Ativo atualizarTituloAPartirDaOperacao(Operacao operacao) {
        def retornoAtualizacao
        if (qtde < 0 || (qtde == 0 && operacao.tipoOperacao.equals(TipoOperacaoEnum.v))) {
            retornoAtualizacao = operacoesAtivo.atualizarTituloOperacaoShort(operacao, this.qtde, this.valorTotalInvestido)
        } else {
            retornoAtualizacao = atualizarTituloOperacaoComum(operacao)
        }
        this.qtde = retornoAtualizacao.qtde
        this.valorTotalInvestido = retornoAtualizacao.valorTotalInvestido

        this
    }

    Ativo atualizarTituloOperacaoComum(Operacao operacao) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            def valorInvestidoEquivalente = valorTotalInvestido.divide(
                    BigDecimal.valueOf(qtde), 4, RoundingMode.HALF_UP) * operacao.qtde
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
}
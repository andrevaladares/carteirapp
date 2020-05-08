package br.com.carteira.entity

import java.math.RoundingMode

trait OperacoesAtivo {

    BigDecimal obterCustoMedio(BigDecimal valorTotalInvestido, Integer qtde) {
        valorTotalInvestido.divide(qtde as BigDecimal, 4, RoundingMode.HALF_UP)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, Integer qtde) {
        valorTotalOperacao - BigDecimal.valueOf(custoMedioVenda * qtde)
    }

    Map atualizarTituloOperacaoShort(Operacao operacao, Integer qtde, BigDecimal valorTotalInvestido) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            qtde -= operacao.qtde
            valorTotalInvestido -= operacao.valorTotalOperacao

        } else {
            def valorMedioShort = valorTotalInvestido.divide(qtde as BigDecimal, 4, RoundingMode.HALF_UP)
            qtde += operacao.qtde
            if (qtde == 0) {
                valorTotalInvestido = 0
            } else {
                def valorAAbater = valorMedioShort * operacao.qtde
                valorTotalInvestido += valorAAbater
            }
        }

        [qtde: qtde, valorTotalInvestido: valorTotalInvestido]
    }
}
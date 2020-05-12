package br.com.carteira.entity

import br.com.carteira.exception.AtivoInvalidoException

import java.math.RoundingMode

trait OperacoesAtivo {

    def validar(Ativo ativo) {
        if(ativo.ticker == null) {
            throw new AtivoInvalidoException("é obrigatório informar o ticker para ativos do tipo: $ativo.tipo")
        }
    }

    BigDecimal obterCustoMedio(BigDecimal valorTotalInvestido, BigDecimal qtde) {
        valorTotalInvestido.divide(qtde as BigDecimal, 4, RoundingMode.HALF_UP)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, BigDecimal qtde) {
        valorTotalOperacao - BigDecimal.valueOf(custoMedioVenda * qtde)
    }

    Map atualizarTituloOperacaoShort(Operacao operacao, BigDecimal qtde, BigDecimal valorTotalInvestido) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            qtde -= operacao.qtde
            valorTotalInvestido -= operacao.valorTotalOperacao

        } else {
            def valorMedioShort = valorTotalInvestido.divide(qtde, 4, RoundingMode.HALF_UP)
            qtde += operacao.qtde
            if (qtde == 0) {
                valorTotalInvestido = 0
            } else {
                def valorAAbater = (valorMedioShort * operacao.qtde).setScale(4, RoundingMode.HALF_UP)
                valorTotalInvestido += valorAAbater
            }
        }

        [qtde: qtde, valorTotalInvestido: valorTotalInvestido]
    }
}
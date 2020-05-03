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

    BigDecimal obterCustoMedio() {
        valorTotalInvestido.divide(qtde as BigDecimal, 4, RoundingMode.HALF_UP)
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Ativo atualizarTituloAPartirDaOperacao(Operacao operacao) {
        if (qtde < 0 || (qtde == 0 && operacao.tipoOperacao.equals(TipoOperacaoEnum.v))) {
            return atualizarTituloOperacaoShort(operacao)
        } else {
            return atualizarTituloOperacaoComum(operacao)
        }
    }

    Ativo atualizarTituloOperacaoShort(Operacao operacao) {
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

        return this
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

}
package br.com.carteira.entity

import java.math.RoundingMode

/**
 * Operações para o contrato de 9,99 gramas de ouro
 */
class OperacoesOuroOz2 implements OperacoesAtivo{

    BigDecimal obterCustoMedio(BigDecimal valorTotalInvestido, BigDecimal qtde) {
        valorTotalInvestido.divide(qtde, 4, RoundingMode.HALF_UP).divide(9.99, 4, RoundingMode.HALF_UP)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, BigDecimal qtde) {
        valorTotalOperacao - custoMedioVenda * qtde * 9.99
    }

}

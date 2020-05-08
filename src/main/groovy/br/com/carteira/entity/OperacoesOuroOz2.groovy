package br.com.carteira.entity

import java.math.RoundingMode

/**
 * Operações para o contrato de 9,99 gramas de ouro
 */
class OperacoesOuroOz2 implements OperacoesAtivo{

    BigDecimal obterCustoMedio(BigDecimal valorTotalInvestido, Integer qtde) {
        valorTotalInvestido.divide(qtde as BigDecimal).divide(9.99, 4, RoundingMode.HALF_UP)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, Integer qtde) {
        valorTotalOperacao - BigDecimal.valueOf(custoMedioVenda * qtde * BigDecimal.valueOf(9.99))
    }

}

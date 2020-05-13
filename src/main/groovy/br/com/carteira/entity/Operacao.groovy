package br.com.carteira.entity

import java.time.LocalDate

class Operacao {
    Long id
    Long idNotaNegociacao
    Long idNotaInvestimento
    TipoOperacaoEnum tipoOperacao
    LocalDate data
    Ativo ativo
    BigDecimal qtde
    BigDecimal valorTotalOperacao
    BigDecimal custoMedioOperacao
    BigDecimal resultadoVenda
}

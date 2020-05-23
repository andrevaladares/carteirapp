package br.com.carteira.entity

import java.time.LocalDate

class Operacao {
    Long id
    Long idNotaNegociacao
    NotaInvestimento notaInvestimento
    TipoOperacaoEnum tipoOperacao
    LocalDate data
    Ativo ativo
    BigDecimal qtde
    BigDecimal valorTotalOperacao
    BigDecimal custoMedioOperacao
    BigDecimal resultadoVenda
}

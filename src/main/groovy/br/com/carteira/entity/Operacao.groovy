package br.com.carteira.entity

import java.time.LocalDate

class Operacao {
    Long id
    Long notaNegociacao
    TipoOperacaoEnum tipoOperacao
    LocalDate data
    Titulo titulo
    Integer qtde
    BigDecimal valorTotalOperacao
    BigDecimal custoMedioVenda
    BigDecimal resultadoVenda
}

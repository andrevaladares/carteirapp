package br.com.carteira.entity

import java.time.LocalDate

class Operacao {
    Long id
    Long idNotaNegociacao
    TipoOperacaoEnum tipoOperacao
    LocalDate data
    Ativo ativo
    Integer qtde
    BigDecimal valorTotalOperacao
    BigDecimal custoMedioVenda
    BigDecimal resultadoVenda
}

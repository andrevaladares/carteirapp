package br.com.carteira.entity

import java.time.LocalDate

class SituacaoCarteira {
    Long id
    LocalDate data
    Long idAtivo
    BigDecimal qtdeDisponivel = 0
    BigDecimal valorInvestido = 0
    BigDecimal valorInvestidoDolares = 0
    BigDecimal valorAtual = 0
    BigDecimal valorAtualDolares = 0

}

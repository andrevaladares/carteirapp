package br.com.carteira.entity

import java.time.LocalDate

class SituacaoCarteira {
    Long id
    LocalDate data
    Long idAtivo
    BigDecimal qtdeDisponivel
    BigDecimal valorInvestido
    BigDecimal valorAtual

}

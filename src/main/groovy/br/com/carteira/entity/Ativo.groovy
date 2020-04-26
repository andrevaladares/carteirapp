package br.com.carteira.entity

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

}
package br.com.carteira.entity

import java.time.LocalDate

class Titulo {
    Long id
    String ticker
    String nome
    TipoTituloEnum tipo
    String setor
    Integer qtde
    BigDecimal valorTotalInvestido
    LocalDate dataEntrada

}
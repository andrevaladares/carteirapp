package br.com.carteira.entity

import java.time.LocalDate

class NotaInvestimento {
    Long id
    LocalDate dataMovimentacao
    String cnpjCorretora
    String nomeCorretora
    RegimeResgateEnum regimeResgate

}

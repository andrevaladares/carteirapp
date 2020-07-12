package br.com.carteira.entity

import java.time.LocalDate

class Operacao {
    Long id
    Long idNotaNegociacao
    NotaInvestimento notaInvestimento
    TipoOperacaoEnum tipoOperacao
    LocalDate data
    Ativo ativo
    BigDecimal qtde = 0.0
    BigDecimal valorTotalOperacao = 0.0
    BigDecimal custoMedioOperacao = 0.0
    BigDecimal resultadoVenda = 0.0
    BigDecimal valorOperacaoDolares = 0.0
    BigDecimal custoMedioDolares = 0.0
    BigDecimal resultadoVendaDolares = 0.0
}

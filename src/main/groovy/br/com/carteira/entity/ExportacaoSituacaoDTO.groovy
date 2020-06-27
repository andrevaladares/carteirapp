package br.com.carteira.entity

import java.time.LocalDate

class ExportacaoSituacaoDTO {
    String ativo
    TipoAtivoEnum tipo
    LocalDate dataEntrada
    LocalDate dataSituacao
    BigDecimal qtde
    BigDecimal valorInvestido
    BigDecimal valorAtual
    BigDecimal rentabilidade
    BigDecimal alocacaoAtual
}

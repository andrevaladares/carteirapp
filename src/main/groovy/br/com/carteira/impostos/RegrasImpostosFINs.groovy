package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult

class RegrasImpostosFINs implements RegrasImpostos{
    TipoAtivoEnum tipoAtivo = TipoAtivoEnum.fin
    BigDecimal percentualImposto = 0.15

    Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        if(prejuizoACompensar > 0) {
            return obterDadosComPrejuizoACompensar(it, prejuizoACompensar)
        }

        return obterDadosSemPrejuizoACompensar(it)
    }
}

package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult

class RegrasImpostosFIIs implements RegrasImpostos{
    TipoAtivoEnum tipoAtivo = TipoAtivoEnum.fii
    BigDecimal percentualImposto = 0.2

    Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        if(prejuizoACompensar > 0) {
            return obterDadosComPrejuizoACompensar(it, prejuizoACompensar)
        }

        return obterDadosSemPrejuizoACompensar(it)
    }
}

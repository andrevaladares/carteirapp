package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult

class RegrasImpostosFIIs implements RegrasImpostos{
    TipoAtivoEnum tipoAtivo = TipoAtivoEnum.fii

    Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        def prejuizoAcumuladoMes
        def impostoDevido
        def baseCalculoImposto

        if(prejuizoACompensar > 0) {
            baseCalculoImposto = it['resultadoTotal'] - prejuizoACompensar
            if (baseCalculoImposto <= 0) {
                prejuizoAcumuladoMes = -baseCalculoImposto
                baseCalculoImposto = 0
                impostoDevido = 0
            } else {
                prejuizoAcumuladoMes = 0
                impostoDevido = baseCalculoImposto * 0.2
            }
            return [
                    'prejuizoAcumuladoMes': prejuizoAcumuladoMes,
                    'baseCalculoImposto': baseCalculoImposto,
                    'impostoDevido': impostoDevido
            ]
        }
        //Prejuizo a compensar = 0
        if (it['resultadoTotal'] < 0) {
            prejuizoAcumuladoMes = -it['resultadoTotal']
            baseCalculoImposto = 0
            impostoDevido = 0
        }
        else {
            prejuizoAcumuladoMes = 0
            baseCalculoImposto = it['resultadoTotal']
            impostoDevido = baseCalculoImposto * 0.2
        }

        return [
                'prejuizoAcumuladoMes': prejuizoAcumuladoMes,
                'baseCalculoImposto': baseCalculoImposto,
                'impostoDevido': impostoDevido
        ]
    }
}

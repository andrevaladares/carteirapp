package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult

class RegrasImpostosAcoesUs implements RegrasImpostos{
    TipoAtivoEnum tipoAtivo = TipoAtivoEnum.aus
    BigDecimal percentualImposto = 0.15

    Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        if (it['valorTotal'] <= 35000) {
            //Isento de imposto... Carrega o prejuizo acumulado pra frente
            return [
                    'prejuizoAcumuladoMes': it['resultadoTotal'] < 0 ? prejuizoACompensar + (-it['resultadoTotal'] as BigDecimal) : prejuizoACompensar,
                    'baseCalculoImposto': 0.00,
                    'impostoDevido': 0.00
            ]

        }

        return obterDadosSemPrejuizoACompensar(it)
    }
}

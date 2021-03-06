package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource

import java.time.YearMonth

trait RegrasImpostos {
    DriverManagerDataSource dataSource

    void consolidarImpostos(YearMonth mesAno, TipoAtivoEnum tipoDeAtivo, DriverManagerDataSource dataSource) {
        this.dataSource = dataSource

        List<GroovyRowResult> somasOperacoesMes = obtemSomasDeOperacoesNoMes(tipoDeAtivo, mesAno)

        def prejuizoMesAnterior = obtemPrejuizoMesAnterior(mesAno, tipoDeAtivo)


        somasOperacoesMes.each {
            if(it['valorTotal']) {
                def valoresImposto = calculaValoresImposto(it, prejuizoMesAnterior['prejuizo_acumulado_mes'] as BigDecimal)

                def qryInsertConsolidacao = """
                insert into consolidacao_impostos_mes (tipo_ativo, data, resultado_mes, valor_total_vendas,
                    prejuizo_acumulado_mes, base_calculo_imposto, imposto_devido)
                values (:tipoAtivo, :data, ${it['resultadoTotal']}, ${it['valorTotal']}, 
                   ${valoresImposto['prejuizoAcumuladoMes']}, ${valoresImposto['baseCalculoImposto']}, 
                   ${valoresImposto['impostoDevido']})
            """

                new Sql(DataSourceUtils.getConnection(dataSource)).executeInsert(['tipoAtivo': tipoDeAtivo as String, 'data': mesAno.atEndOfMonth()], qryInsertConsolidacao)

            }
        }

    }

    GroovyRowResult obtemPrejuizoMesAnterior(YearMonth mesAno, TipoAtivoEnum tipoDeAtivo) {
        def qryPrejuizoMesAnterior = """
            select tipo_ativo, data, prejuizo_acumulado_mes
            from consolidacao_impostos_mes
            where data = (select max(data) from consolidacao_impostos_mes where tipo_ativo = ${tipoDeAtivo as String})
                and tipo_ativo = ${tipoDeAtivo as String}
        """
        def prejuizosMesAnterior = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(qryPrejuizoMesAnterior)
        prejuizosMesAnterior
    }

    List<GroovyRowResult> obtemSomasDeOperacoesNoMes(TipoAtivoEnum tipoDeAtivo, YearMonth mesAno) {
        def qryValoresConsolidadosMes = """
            select  sum(op.valor_total_operacao) as valorTotal, sum(op.resultado_venda) as resultadoTotal, 
                    sum(op.resultado_venda_dolares) resultadoTotalDolares
            from operacao op
            inner join ativo at on op.ativo = at.id
            where at.tipo = '${tipoDeAtivo as String}'
                  and month(op.data) = :mes and year(op.data) = :ano
                  and op.custo_medio_operacao is not null and op.custo_medio_operacao <> 0
                  and op.resultado_venda is not null and op.resultado_venda <> 0
        """
        def somasOperacoesMes = new Sql(DataSourceUtils.getConnection(dataSource)).
                rows(['mes': mesAno.monthValue, 'ano': mesAno.year], qryValoresConsolidadosMes)
        somasOperacoesMes
    }

    Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {

        if (it['valorTotal'] <= 20000) {
            //Isento de imposto... Carrega o prejuizo acumulado pra frente
            return [
                    'prejuizoAcumuladoMes': it['resultadoTotal'] < 0 ? prejuizoACompensar + (-it['resultadoTotal'] as BigDecimal) : prejuizoACompensar,
                    'baseCalculoImposto': 0.00,
                    'impostoDevido': 0.00
            ]

        }

        if(prejuizoACompensar > 0) {
            return obterDadosComPrejuizoACompensar(it, prejuizoACompensar)
        }


        return obterDadosSemPrejuizoACompensar(it)
    }

    Map obterDadosSemPrejuizoACompensar(GroovyRowResult it) {
        //Prejuizo a compensar = 0

        def prejuizoAcumuladoMes, baseCalculoImposto, impostoDevido
        if (it['resultadoTotal'] < 0) {
            prejuizoAcumuladoMes = -it['resultadoTotal']
            baseCalculoImposto = 0
            impostoDevido = 0
        } else {
            prejuizoAcumuladoMes = 0
            baseCalculoImposto = it['resultadoTotal']
            impostoDevido = baseCalculoImposto * this.percentualImposto
        }

        return [
                'prejuizoAcumuladoMes': prejuizoAcumuladoMes,
                'baseCalculoImposto'  : baseCalculoImposto,
                'impostoDevido'       : impostoDevido
        ]
    }

    Map obterDadosComPrejuizoACompensar(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        def prejuizoAcumuladoMes, impostoDevido, baseCalculoImposto
        baseCalculoImposto = it['resultadoTotal'] - prejuizoACompensar
        if (baseCalculoImposto <= 0) {
            prejuizoAcumuladoMes = -baseCalculoImposto
            baseCalculoImposto = 0
            impostoDevido = 0
        } else {
            prejuizoAcumuladoMes = 0
            impostoDevido = baseCalculoImposto * this.percentualImposto
        }
        ['prejuizoAcumuladoMes': prejuizoAcumuladoMes,
         'baseCalculoImposto': baseCalculoImposto,
         'impostoDevido': impostoDevido]
    }

    abstract BigDecimal getPercentualImposto()
    abstract TipoAtivoEnum getTipoAtivo()
}
package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.time.YearMonth

@Component
@Transactional(readOnly = true)
class ConsolidacaoImpostos {

    DriverManagerDataSource dataSource


    @Autowired
    ConsolidacaoImpostos(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    @Transactional
    void consolidarImpostos(YearMonth mesAno, TipoAtivoEnum tipoDeAtivo) {
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

    private GroovyRowResult obtemPrejuizoMesAnterior(YearMonth mesAno, TipoAtivoEnum tipoDeAtivo) {
        def qryPrejuizoMesAnterior = """
            select tipo_ativo, data, prejuizo_acumulado_mes
            from consolidacao_impostos_mes
            where data = ${mesAno.minusMonths(1).atEndOfMonth()}
                and tipo_ativo = ${tipoDeAtivo as String}
        """
        def prejuizosMesAnterior = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(qryPrejuizoMesAnterior)
        prejuizosMesAnterior
    }

    private List<GroovyRowResult> obtemSomasDeOperacoesNoMes(TipoAtivoEnum tipoDeAtivo, YearMonth mesAno) {
        def qryValoresConsolidadosMes = """
            select  sum(op.valor_total_operacao) as valorTotal, sum(op.resultado_venda) as resultadoTotal, 
                    sum(op.resultado_venda_dolares) resultadoTotalDolares
            from operacao op
            inner join ativo at on op.ativo = at.id
            where at.tipo = '${tipoDeAtivo as String}'
                  and month(op.data) = :mes and year(op.data) = :ano
                  and op.tipo_operacao = 'v'
        """
        def somasOperacoesMes = new Sql(DataSourceUtils.getConnection(dataSource)).
                rows(['mes': mesAno.monthValue, 'ano': mesAno.year], qryValoresConsolidadosMes)
        somasOperacoesMes
    }

    private Map calculaValoresImposto(GroovyRowResult it, BigDecimal prejuizoACompensar) {
        def prejuizoAcumuladoMes
        def impostoDevido
        def baseCalculoImposto
        if (it['valorTotal'] <= 20000) {
            //Isento de imposto... Carrega o prejuizo acumulado pra frente
            return [
                    'prejuizoAcumuladoMes': prejuizoACompensar,
                    'baseCalculoImposto': 0.00,
                    'impostoDevido': 0.00
            ]

        }

        if(prejuizoACompensar > 0) {
            baseCalculoImposto = it['resultadoTotal'] - prejuizoACompensar
            if (baseCalculoImposto <= 0) {
                prejuizoAcumuladoMes = -baseCalculoImposto
                baseCalculoImposto = 0
                impostoDevido = 0
            } else {
                prejuizoAcumuladoMes = 0
                impostoDevido = baseCalculoImposto * 0.15
            }
            return [
                    'prejuizoAcumuladoMes': prejuizoAcumuladoMes,
                    'baseCalculoImposto': baseCalculoImposto,
                    'impostoDevido': impostoDevido
            ]
        }
        //Prejuizo a compensar = 0
        prejuizoAcumuladoMes = 0
        baseCalculoImposto = it['resultadoTotal']
        impostoDevido = baseCalculoImposto * 0.15

        return [
                'prejuizoAcumuladoMes': prejuizoAcumuladoMes,
                'baseCalculoImposto': baseCalculoImposto,
                'impostoDevido': impostoDevido
        ]
    }

    GroovyRowResult obterConsolidacao(YearMonth mesAnoReferencia, TipoAtivoEnum tipoDeAtivo) {
        def query = '''
            select * from consolidacao_impostos_mes
            where data = :dataReferencia and tipo_ativo = :tipoAtivo
        '''
        new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['dataReferencia': mesAnoReferencia.atEndOfMonth(),
                                                                     'tipoAtivo': tipoDeAtivo as String], query)
    }
}

package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.time.LocalDate
import java.time.YearMonth

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
class ConsolidacaoImpostosIT {
    @Autowired
    private ConsolidacaoImpostos consolidacaoImpostos

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql"])
    void 'gera consolidacao sem operacoes no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraConsolidacao.sql"])
    void 'gera consolidacao apenas operacoes de compra no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaMenos20000Consolidacao.sql"])
    void 'gera consolidacao operacoes de compra e venda com isencao' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert consolidacao['tipo_ativo'] == 'a'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 1633.33
        assert consolidacao['valor_total_vendas'] == 19500.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 0.00
        assert consolidacao['imposto_devido'] == 0.00
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaMaisQue20000Consolidacao.sql"])
    void 'gera consolidacao operacoes de compra e venda com imposto devido' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)
        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert consolidacao['tipo_ativo'] == 'a'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 20001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 2134.33
        assert consolidacao['imposto_devido'] == 320.15
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaCompensandoPrejuizo.sql"])
    void 'gera consolidacao operacoes de compra e venda abatendo prejuizo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert consolidacao['tipo_ativo'] == 'a'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 20001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 1134.33
        assert consolidacao['imposto_devido'] == 170.15
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesPrejuizoAcumuladoMaiorQueResultado.sql"])
    void 'gera consolidacao operacoes de compra e venda prejuizo acumulado abate todo o resultado' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.a)

        assert consolidacao['tipo_ativo'] == 'a'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 20001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 865.67
        assert consolidacao['base_calculo_imposto'] == 0.00
        assert consolidacao['imposto_devido'] == 0.00
    }

}

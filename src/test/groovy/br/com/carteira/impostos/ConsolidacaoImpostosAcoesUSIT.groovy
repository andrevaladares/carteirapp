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
class ConsolidacaoImpostosAcoesUSIT {
    @Autowired
    private ConsolidacaoImpostos consolidacaoImpostos

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql"])
    void 'gera consolidacao sem operacoes no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraConsolidacaoUS.sql"])
    void 'gera consolidacao apenas operacoes de compra no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaMenos35000ConsolidacaoUS.sql"])
    void 'gera consolidacao operacoes de compra e venda com isencao' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert consolidacao['tipo_ativo'] == 'aus'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 1633.33
        assert consolidacao['valor_total_vendas'] == 34999.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 0.00
        assert consolidacao['imposto_devido'] == 0.00
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaMaisQue35000ConsolidacaoUS.sql"])
    void 'gera consolidacao operacoes de compra e venda com imposto devido' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)
        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert consolidacao['tipo_ativo'] == 'aus'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 1633.33
        assert consolidacao['valor_total_vendas'] == 35001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 1633.33
        assert consolidacao['imposto_devido'] == 245.00
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaSemAbaterPrejuizoUS.sql"])
    void 'gera consolidacao operacoes de compra e venda sem abater prejuizo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert consolidacao['tipo_ativo'] == 'aus'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 35001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 2134.33
        assert consolidacao['imposto_devido'] == 320.15
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesAcoesPrejuizoMesUS.sql"])
    void 'gera consolidacao operacoes de compra e venda gerando prejuizo no mes' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.aus)

        assert consolidacao['tipo_ativo'] == 'aus'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == -1867.00
        assert consolidacao['valor_total_vendas'] == 16000.00
        assert consolidacao['prejuizo_acumulado_mes'] == 1867.00
        assert consolidacao['base_calculo_imposto'] == 0.00
        assert consolidacao['imposto_devido'] == 0.00
    }

}

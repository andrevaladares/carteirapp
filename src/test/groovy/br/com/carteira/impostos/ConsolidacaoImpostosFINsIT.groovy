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
class ConsolidacaoImpostosFINsIT {
    @Autowired
    private ConsolidacaoImpostos consolidacaoImpostos

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql"])
    void 'gera consolidacao sem operacoes no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraConsolidacaoFin.sql"])
    void 'gera consolidacao apenas operacoes de compra no periodo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)
        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert !consolidacao
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompraEVendaMenos20000FINs.sql"])
    void 'gera consolidacao operacoes imposto mesmo abaixo de 20000' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert consolidacao['tipo_ativo'] == 'fin'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 1633.33
        assert consolidacao['valor_total_vendas'] == 19500.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 1633.33
        assert consolidacao['imposto_devido'] == 245.00
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesMaisQue20000FINs.sql"])
    void 'gera consolidacao operacoes de compra e venda com imposto devido acima de 20000' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)
        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert consolidacao['tipo_ativo'] == 'fin'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 20001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 2134.33
        assert consolidacao['imposto_devido'] == 320.15
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesCompensandoPrejuizoFins.sql"])
    void 'gera consolidacao operacoes de compra e venda abatendo prejuizo' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert consolidacao['tipo_ativo'] == 'fin'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 2134.33
        assert consolidacao['valor_total_vendas'] == 20001.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 1134.33
        assert consolidacao['imposto_devido'] == 170.15
    }

      @Test
      @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesPrejuizoAcumuladoMaiorQueResultadoFins.sql"])
      void 'gera consolidacao operacoes de compra e venda prejuizo acumulado abate todo o resultado' () {
          consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          assert consolidacao['tipo_ativo'] == 'fin'
          assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
          assert consolidacao['resultado_mes'] == 2134.33
          assert consolidacao['valor_total_vendas'] == 20001.00
          assert consolidacao['prejuizo_acumulado_mes'] == 865.67
          assert consolidacao['base_calculo_imposto'] == 0.00
          assert consolidacao['imposto_devido'] == 0.00
      }

      @Test
      @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesAcumulamPrejuizoNoMesFins.sql"])
      void 'gera consolidacao operacoes de compra e venda acumula prejuizo no mes' () {
          consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          assert consolidacao['tipo_ativo'] == 'fin'
          assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
          assert consolidacao['resultado_mes'] == -6867.00
          assert consolidacao['valor_total_vendas'] == 11000.00
          assert consolidacao['prejuizo_acumulado_mes'] == 9867.00
          assert consolidacao['base_calculo_imposto'] == 0.00
          assert consolidacao['imposto_devido'] == 0.00
      }

      @Test
      @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesGeramPrejuizoNoMesFins.sql"])
      void 'gera consolidacao operacoes de compra e venda gerando prejuizo no mes' () {
          consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          GroovyRowResult consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

          assert consolidacao['tipo_ativo'] == 'fin'
          assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
          assert consolidacao['resultado_mes'] == -6867.00
          assert consolidacao['valor_total_vendas'] == 11000.00
          assert consolidacao['prejuizo_acumulado_mes'] == 6867.00
          assert consolidacao['base_calculo_imposto'] == 0.00
          assert consolidacao['imposto_devido'] == 0.00
      }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesVendaDescobertaFins.sql"])
    void 'operacao de venda descoberta nao gera consolidacao' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert !consolidacao

    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:operacoesReducaoShortComLucroFins.sql"])
    void 'operacao de reducao de short com lucro gera imposto' () {
        consolidacaoImpostos.consolidarImpostos(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        def consolidacao = consolidacaoImpostos.obterConsolidacao(YearMonth.of(2020, 1), TipoAtivoEnum.fin)

        assert consolidacao['tipo_ativo'] == 'fin'
        assert consolidacao['data'].toLocalDate() == LocalDate.of(2020, 1, 31)
        assert consolidacao['resultado_mes'] == 5500.00
        assert consolidacao['valor_total_vendas'] == 30000.00
        assert consolidacao['prejuizo_acumulado_mes'] == 0.00
        assert consolidacao['base_calculo_imposto'] == 5500.00
        assert consolidacao['imposto_devido'] == 825.00

    }

}

package br.com.carteira.service

import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.repository.NotaNegociacaoRepository
import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.AtivoRepository
import groovy.sql.GroovyRowResult
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.time.LocalDate

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
@Sql(scripts = ["classpath:limpaDados.sql"])
class ImportarNotaEOperacaoIT {

    @Autowired
    OperacaoService operacaoService
    @Autowired
    NotaNegociacaoRepository notaNegociacaoRepository
    @Autowired
    AtivoRepository tituloRepository
    @Autowired
    OperacaoRepository operacaoRepository

    @Test
    void "importa corretamente a nota e suas operacoes"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTeste.txt'

        operacaoService.importarArquivoNotaNegociacao(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        Assert.assertEquals(new BigDecimal('14.42'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('1.92'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('170.10'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('18.16'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('1.32'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('6.63'), notaNegociacaoGravada.outrosCustos)

        def aper3f = tituloRepository.getByTicker('aper3f')
        def bpan4 = tituloRepository.getByTicker('bpan4')

        def dataOperacoes = LocalDate.of(2020, 2, 3)
        //Saldos dos títulos determinados corretamente
        Assert.assertEquals(dataOperacoes, aper3f.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, aper3f.tipo)
        Assert.assertEquals(30, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('802.14'), aper3f.valorTotalInvestido)
        Assert.assertEquals(dataOperacoes, bpan4.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, bpan4.tipo)
        Assert.assertEquals(300, bpan4.qtde)
        Assert.assertEquals(new BigDecimal('1215.26'), bpan4.valorTotalInvestido)

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesAper3 = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'aper3f')
        def operacaoCompra = operacoesAper3.find {it['tipo_operacao'] == 'c'}
        Assert.assertEquals(new BigDecimal('1069.52'), operacaoCompra['valor_total_operacao'])
        def operacaoVenda = operacoesAper3.find {it['tipo_operacao'] == 'v'}
        Assert.assertEquals(new BigDecimal('270.00'), operacaoVenda['valor_total_operacao'])
    }

    @Test
    void "aloca corretamente os custos quando há somente operação de venda (short)"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTesteVendas.txt'

        operacaoService.importarArquivoNotaNegociacao(caminhoArquivo, nomeArquivo)

        def aper3f = tituloRepository.getByTicker('aper3f')
        def bpan4 = tituloRepository.getByTicker('bpan4')

        def dataOperacoes = LocalDate.of(2020, 2, 3)
        Assert.assertEquals(dataOperacoes, aper3f.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, aper3f.tipo)
        Assert.assertEquals(-40, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('-1069.52'), aper3f.valorTotalInvestido)
        Assert.assertEquals(dataOperacoes, bpan4.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, bpan4.tipo)
        Assert.assertEquals(-300, bpan4.qtde)
        Assert.assertEquals(new BigDecimal('-1215.26'), bpan4.valorTotalInvestido)
    }

}

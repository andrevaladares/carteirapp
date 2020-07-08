package br.com.carteira.service

import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.OperacaoInvalidaException
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
    AtivoRepository ativoRepository
    @Autowired
    OperacaoRepository operacaoRepository

    @Test
    void "importa corretamente a nota e suas operacoes"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTeste.txt'

        operacaoService.importarOperacoesNotaNegociacao(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        Assert.assertEquals(new BigDecimal('14.42'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('1.92'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('170.10'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('18.16'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('1.32'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('6.63'), notaNegociacaoGravada.outrosCustos)

        def aper3f = ativoRepository.getByTicker('aper3f')
        def bpan4 = ativoRepository.getByTicker('bpan4')

        def dataOperacoes = LocalDate.of(2020, 2, 3)
        //Saldos dos títulos determinados corretamente
        Assert.assertEquals(dataOperacoes, aper3f.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, aper3f.tipo)
        Assert.assertEquals(30.00000000, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('802.14'), aper3f.valorTotalInvestido)
        Assert.assertEquals(dataOperacoes, bpan4.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, bpan4.tipo)
        Assert.assertEquals(300.00000000, bpan4.qtde)
        Assert.assertEquals(new BigDecimal('1215.26'), bpan4.valorTotalInvestido)

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesAper3 = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'aper3f')
        def operacaoCompra = operacoesAper3.find {it['tipo_operacao'] == 'c'}
        Assert.assertEquals(new BigDecimal('1069.52'), operacaoCompra['valor_total_operacao'])
        def operacaoVenda = operacoesAper3.find {it['tipo_operacao'] == 'v'}
        Assert.assertEquals(new BigDecimal('270.00'), operacaoVenda['valor_total_operacao'])
    }

    @Test
    void "aloca corretamente os custos quando ha somente operacao de venda (short)"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTesteVendas.txt'

        operacaoService.importarOperacoesNotaNegociacao(caminhoArquivo, nomeArquivo)

        def aper3f = ativoRepository.getByTicker('aper3f')
        def bpan4 = ativoRepository.getByTicker('bpan4')

        def dataOperacoes = LocalDate.of(2020, 2, 3)
        Assert.assertEquals(dataOperacoes, aper3f.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, aper3f.tipo)
        Assert.assertEquals(-40.00000000, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('-1069.52'), aper3f.valorTotalInvestido)
        Assert.assertEquals(dataOperacoes, bpan4.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.a, bpan4.tipo)
        Assert.assertEquals(-300.00000000, bpan4.qtde)
        Assert.assertEquals(new BigDecimal('-1215.26'), bpan4.valorTotalInvestido)
    }

    @Test
    void "importa corretamente nota de negociacao de ouro com compra e venda" () {
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaNegociacaoOuro_teste.txt'

        operacaoService.importarOperacoesNotaNegociacao(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('260.00'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('27.76'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.outrosCustos)
        Assert.assertEquals(new BigDecimal('2.38'), notaNegociacaoGravada.taxaRegistroBmf)
        Assert.assertEquals(new BigDecimal('0.78'), notaNegociacaoGravada.taxasBmfEmolFgar)

        def oz2 = ativoRepository.getByTicker('oz2')

        def dataOperacoes = LocalDate.of(2020, 3, 9)
        //Saldos dos títulos determinados corretamente
        Assert.assertEquals(dataOperacoes, oz2.dataEntrada)
        Assert.assertEquals(TipoAtivoEnum.oz2, oz2.tipo)
        Assert.assertEquals(10.00000000, oz2.qtde)
        Assert.assertEquals(new BigDecimal('25882.70'), oz2.valorTotalInvestido)

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesOz2 = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'oz2')
        def operacaoCompra = operacoesOz2.find {it['tipo_operacao'] == 'c'}
        Assert.assertEquals(new BigDecimal('23308.28'), operacaoCompra['valor_total_operacao'])
        def operacaoVenda = operacoesOz2.find {it['tipo_operacao'] == 'v'}
        Assert.assertEquals(new BigDecimal('7792.20'), operacaoVenda['valor_total_operacao'])
        Assert.assertEquals(new BigDecimal('259.08610000'), operacaoVenda['custo_medio_operacao'])
        Assert.assertEquals(new BigDecimal('27.39'), operacaoVenda['resultado_venda'])
    }

    @Test
    void "importa corretamente nota de negociacao de compra de dolar" () {
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaNegociacaoDolar_teste.txt'

        operacaoService.importarOperacoesNotaNegociacao(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.outrosCustos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaRegistroBmf)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxasBmfEmolFgar)

        def dolar = ativoRepository.getByTicker('us$')

        def dataOperacoes = LocalDate.of(2019, 12, 9)
        //Saldos dos títulos determinados corretamente
        assert dolar.dataEntrada == dataOperacoes
        assert dolar.tipo == TipoAtivoEnum.m
        assert dolar.qtde == 1178.34
        assert dolar.valorTotalInvestido == 5000.00

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesDolar = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'us$')
        def operacaoCompra = operacoesDolar.find {it['tipo_operacao'] == 'c'}
        assert operacaoCompra['valor_total_operacao'] == new BigDecimal('5000.00')
    }

    @Test
    void "erro ao tentar importar operacao de compra de acao internacional sem dolares em carteira" () {
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaNegociacaoAcaoUs_teste.txt'

        try {
            operacaoService.importarOperacoesNotaNegociacaoUs(caminhoArquivo, nomeArquivo)
        }
        catch (OperacaoInvalidaException ex) {
            assert ex.getMessage() == 'Não é permitido comprar ações US sem dolares disponíveis em carteira'
        }
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:ativosDolares.sql"])
    void "importa corretamente nota de negociacao de compra de acao estados unidos" () {
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaNegociacaoAcaoUs_teste.txt'

        operacaoService.importarOperacoesNotaNegociacaoUs(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.outrosCustos)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxaRegistroBmf)
        Assert.assertEquals(new BigDecimal('0.00'), notaNegociacaoGravada.taxasBmfEmolFgar)

        def dolar = ativoRepository.getByTicker('us$')
        def acaoXP = ativoRepository.getByTicker('xp')

        def dataOperacoes = LocalDate.of(2019, 12, 13)
        //Saldos dos títulos determinados corretamente
        assert dolar.tipo == TipoAtivoEnum.m
        assert dolar.qtde == 962.11
        assert dolar.valorTotalInvestido == 5051.08
        assert acaoXP.tipo == TipoAtivoEnum.aus
        assert acaoXP.qtde == 30
        assert acaoXP.valorTotalInvestido == 1037.89

        //operações determinadas corretamente
        def transferenciaSaidaDolar = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'us$')[0]
        assert transferenciaSaidaDolar['valor_total_operacao'] == 5448.92
        assert transferenciaSaidaDolar['tipo_operacao'] == TipoOperacaoEnum.ts as String
        assert transferenciaSaidaDolar['qtde'] == 1037.89
        assert transferenciaSaidaDolar['ativo'] == dolar.id

        def operacaoCompraXP = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'xp')[0]
        assert operacaoCompraXP['valor_total_operacao'] == 1037.89
        assert operacaoCompraXP['tipo_operacao'] == TipoOperacaoEnum.c as String
        assert operacaoCompraXP['qtde'] == 30
        assert operacaoCompraXP['ativo'] == acaoXP.id

    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:ativosTesteVendaAcaoIntl.sql"])
    void "importa corretamente nota de negociacao de venda de acao estados unidos" () {
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaNegociacaoAcaoUs_testeVenda.txt'

        operacaoService.importarOperacoesNotaNegociacaoUs(caminhoArquivo, nomeArquivo)

        def dolar = ativoRepository.getByTicker('us$')
        def acaoXP = ativoRepository.getByTicker('xp')

        def dataOperacoes = LocalDate.of(2019, 12, 13)
        //Saldos dos títulos determinados corretamente
        assert dolar.tipo == TipoAtivoEnum.m
        assert dolar.qtde == 2345.96
        assert dolar.valorTotalInvestido == 12316.29
        assert acaoXP.tipo == TipoAtivoEnum.aus
        assert acaoXP.qtde == 20
        assert acaoXP.valorTotalInvestido == 693.33

        //operações determinadas corretamente
        def transferenciaEntradaDolar = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'us$')[0]
        assert transferenciaEntradaDolar['valor_total_operacao'] == 1816.29
        assert transferenciaEntradaDolar['tipo_operacao'] == TipoOperacaoEnum.te as String
        assert transferenciaEntradaDolar['qtde'] == 345.96
        assert transferenciaEntradaDolar['ativo'] == dolar.id

        def operacaoVendaXP = operacaoRepository.getByDataOperacaoTicker(dataOperacoes, 'xp')[0]
        assert operacaoVendaXP['valor_total_operacao'] == 345.96
        assert operacaoVendaXP['tipo_operacao'] == TipoOperacaoEnum.v as String
        assert operacaoVendaXP['qtde'] == 10
        assert operacaoVendaXP['ativo'] == acaoXP.id
        assert operacaoVendaXP['custo_medio_operacao'] == 34.66666667
        assert operacaoVendaXP['resultado_venda'] == -0.71

    }

}

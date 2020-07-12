package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.NotaInvestimentoRepository
import br.com.carteira.repository.OperacaoRepository
import groovy.sql.GroovyRowResult
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
class ImportarNotaInvestimentoIT {

    @Autowired
    OperacaoService operacaoService
    @Autowired
    NotaInvestimentoRepository notaInvestimentoRepository
    @Autowired
    AtivoRepository ativoRepository
    @Autowired
    OperacaoRepository operacaoRepository

    @Test
    void "importa corretamente uma nota de fundo cambial e suas operacoes"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoCambial_teste.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def notaInvestimentoGravada = notaInvestimentoRepository.fromNotaInvestimentoGroovyRow(notaInvestimentoRepository.listAll()[0])

        //Dados gerais da nota gravados corretamente
        assert notaInvestimentoGravada.dataMovimentacao == LocalDate.of(2019, 12, 20)
        assert notaInvestimentoGravada.cnpjCorretora == '02332886000104'
        assert notaInvestimentoGravada.nomeCorretora == 'XP'

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150', 'asc')

        def dataOperacoes = LocalDate.of(2019, 12, 20)
        //Saldos dos títulos determinados corretamente
        assert dataOperacoes == fundoCambialList[0].dataEntrada
        assert TipoAtivoEnum.fiv == fundoCambialList[0].tipo
        assert 936.9351 == fundoCambialList[0].qtde
        assert new BigDecimal('3000.00') == fundoCambialList[0].valorTotalInvestido

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        def operacaoCompra = operacoesFundoCambial.find {it['tipo_operacao'] == 'c'}
        assert new BigDecimal('3000.00') == operacaoCompra['valor_total_operacao']
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:dadosTesteVendaFundoCambial.sql"])
    void "importa corretamente uma nota de venda de fundo cambial e suas operacoes"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoCambialVenda_teste.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150', 'asc')

        def dataOperacoes = LocalDate.of(2020, 2, 27)
        //Saldos dos títulos determinados corretamente
        assert fundoCambialList[0].qtde == 729.1147
        assert fundoCambialList[0].valorTotalInvestido == 2334.57

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        def operacaoCompra = operacoesFundoCambial.find {it['tipo_operacao'] == 'v'}
        assert operacaoCompra['valor_total_operacao'] == new BigDecimal('800.00')
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:dadosTesteVendaFundoLifoFifo.sql"])
    void "realiza corretamente operacao de venda de fundo lifo"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoVendaLifo.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150', 'desc')

        def dataOperacoes = LocalDate.of(2020, 2, 27)
        //Saldos dos títulos determinados corretamente. A lista não contem o último título, que teve o valor zerado
        assert fundoCambialList[0].qtde == 286
        assert fundoCambialList[0].valorTotalInvestido == 858
        assert fundoCambialList[1].qtde == 936.9351
        assert fundoCambialList[1].valorTotalInvestido == 3000

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        assert operacoesFundoCambial[0]['qtde'] == new BigDecimal('300')
        assert operacoesFundoCambial[0]['valor_total_operacao'] == new BigDecimal('1159.42')
        assert operacoesFundoCambial[0]['custo_medio_operacao'] == new BigDecimal('3.5')
        assert operacoesFundoCambial[0]['resultado_venda'] == new BigDecimal('109.42')
        assert operacoesFundoCambial[1]['qtde'] == new BigDecimal('114')
        assert operacoesFundoCambial[1]['valor_total_operacao'] == new BigDecimal('440.58')
        assert operacoesFundoCambial[1]['custo_medio_operacao'] == new BigDecimal('3')
        assert operacoesFundoCambial[1]['resultado_venda'] == new BigDecimal('98.58')
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:dadosTesteVendaFundoLifoFifo.sql"])
    void "realiza corretamente operacao de venda de fundo fifo"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoVendaFifo.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150', 'asc')

        def dataOperacoes = LocalDate.of(2020, 2, 27)
        //Saldos dos títulos determinados corretamente. A lista não contem o último título, que teve o valor zerado
        assert fundoCambialList[0].qtde == 522.9351
        assert fundoCambialList[0].valorTotalInvestido == 1674.40
        assert fundoCambialList[1].qtde == 400
        assert fundoCambialList[1].valorTotalInvestido == 1200
        assert fundoCambialList[2].qtde == 300
        assert fundoCambialList[2].valorTotalInvestido == 1050

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        assert operacoesFundoCambial[0]['qtde'] == new BigDecimal('414')
        assert operacoesFundoCambial[0]['valor_total_operacao'] == new BigDecimal('1600')
        assert operacoesFundoCambial[0]['custo_medio_operacao'] == new BigDecimal('3.20192935')
        assert operacoesFundoCambial[0]['resultado_venda'] == new BigDecimal('274.40')
    }

    @Test
    void "realiza corretamente compra de tesouro direto"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoTesouro.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def ativoExemplo = Ativo.getInstanceWithAtributeMap(
                nome: 'Tesouro IPCA+ com Juros Semestrais 2050'
        )

        def listOfAtivos = ativoRepository.getAllByAtivoExample(ativoExemplo, 'asc')

        def dataOperacoes = LocalDate.of(2019, 12, 20)
        //Saldos dos títulos determinados corretamente. A lista não contem o último título, que teve o valor zerado
        assert listOfAtivos.size() == 1
        assert listOfAtivos[0].qtde == 3.91
        assert listOfAtivos[0].valorTotalInvestido == 18873.10

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoes = operacaoRepository.getByDataOperacaoNomeAtivo(dataOperacoes, 'Tesouro IPCA+ com Juros Semestrais 2050')
        assert operacoes[0]['qtde'] == 3.91
        assert operacoes[0]['valor_total_operacao'] == 18873.10
        assert operacoes[0]['custo_medio_operacao'] == 4826.87979540
        assert operacoes[0]['resultado_venda'] == 0.0
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:dadosTesteVendaTesouroFifo.sql"])
    void "realiza corretamente venda de tesouro direto fifo"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaInvestimentoVendaTesouro.txt'

        operacaoService.importarOperacoesNotaInvestimento(caminhoArquivo, nomeArquivo)

        def ativoExemplo = Ativo.getInstanceWithAtributeMap(nome: 'Tesouro IPCA+ com Juros Semestrais 2050')

        def listOfAtivos = ativoRepository.getAllByAtivoExample(ativoExemplo, 'asc')

        def dataOperacoes = LocalDate.of(2019, 12, 25)
        //Saldos dos títulos determinados corretamente. A lista não contem o último título, que teve o valor zerado
        assert listOfAtivos.size() == 1
        assert listOfAtivos[0].qtde == 1.3351
        assert listOfAtivos[0].valorTotalInvestido == 1068.08

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoes = operacaoRepository.getByDataOperacaoNomeAtivo(dataOperacoes, 'Tesouro IPCA+ com Juros Semestrais 2050')
        assert operacoes.size() == 2
        assert operacoes[0]['qtde'] == 3.9351
        assert operacoes[0]['valor_total_operacao'] == 18235.83
        assert operacoes[0]['custo_medio_operacao'] == 1524.73888847
        assert operacoes[0]['resultado_venda'] == 12235.83
        assert operacoes[1]['qtde'] == 0.1649
        assert operacoes[1]['valor_total_operacao'] == 764.17
        assert operacoes[1]['custo_medio_operacao'] == 800
        assert operacoes[1]['resultado_venda'] == 632.25
    }

}

package br.com.carteira.service

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

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150')

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

        def fundoCambialList = ativoRepository.getAllByCnpjFundo('3319016000150')

        def dataOperacoes = LocalDate.of(2020, 2, 27)
        //Saldos dos títulos determinados corretamente
        assert fundoCambialList[0].qtde == 729.1147
        assert fundoCambialList[0].valorTotalInvestido == 2334.57

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        def operacaoCompra = operacoesFundoCambial.find {it['tipo_operacao'] == 'v'}
        assert operacaoCompra['valor_total_operacao'] == new BigDecimal('800.00')
    }

}

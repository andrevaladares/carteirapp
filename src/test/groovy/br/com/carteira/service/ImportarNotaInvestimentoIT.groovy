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

        def fundoCambial = ativoRepository.getByCnpjFundo('3319016000150')

        def dataOperacoes = LocalDate.of(2019, 12, 20)
        //Saldos dos títulos determinados corretamente
        assert dataOperacoes == fundoCambial.dataEntrada
        assert TipoAtivoEnum.fiv == fundoCambial.tipo
        assert 936.9351 == fundoCambial.qtde
        assert new BigDecimal('3000.00') == fundoCambial.valorTotalInvestido

        //Valor de operações calculados corretamente em função dos custos
        List<GroovyRowResult> operacoesFundoCambial = operacaoRepository.getByDataOperacaoCnpjFundo(dataOperacoes, '3319016000150')
        def operacaoCompra = operacoesFundoCambial.find {it['tipo_operacao'] == 'c'}
        assert new BigDecimal('3000.00') == operacaoCompra['valor_total_operacao']
    }

}

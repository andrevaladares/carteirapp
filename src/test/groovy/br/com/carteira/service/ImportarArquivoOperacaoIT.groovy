package br.com.carteira.service

import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.TituloRepository
import org.junit.Assert
import org.junit.Ignore
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
class ImportarArquivoOperacaoIT {

    @Autowired
    OperacaoService operacaoService
    @Autowired
    OperacaoRepository operacaoRepository
    @Autowired
    TituloRepository tituloRepository

    @Test
    void importaArquivoSucesso(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'operacaoesAcoes1.txt'

        operacaoService.importarArquivoOperacao(caminhoArquivo, nomeArquivo)

        def operacoes = operacaoRepository.findAll()
        Assert.assertEquals(3, operacoes.size())

        def aper3f = tituloRepository.getByTicker('aper3f')
        def also3f = tituloRepository.getByTicker('also3f')
        Assert.assertEquals(0, also3f.qtde)
        Assert.assertEquals(new BigDecimal('0.00'), also3f.valorTotalInvestido)
        Assert.assertEquals(LocalDate.of(2019, 4, 5), also3f.dataEntrada)
        Assert.assertEquals(40, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('1047.83'), aper3f.valorTotalInvestido)
        Assert.assertEquals(LocalDate.of(2019, 3, 18), aper3f.dataEntrada)
    }

}

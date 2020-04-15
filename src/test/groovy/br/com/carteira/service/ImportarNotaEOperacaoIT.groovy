package br.com.carteira.service

import br.com.carteira.entity.TipoTituloEnum
import br.com.carteira.repository.NotaNegociacaoRepository
import br.com.carteira.repository.TituloRepository
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
    TituloRepository tituloRepository

    @Test
    void "importa corretamente a nota e suas operacoes"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTeste.txt'

        operacaoService.importarArquivoNotaNegociacao(caminhoArquivo, nomeArquivo)

        def notaNegociacaoGravada = notaNegociacaoRepository.fromNotaNegociacaoGroovyRow(notaNegociacaoRepository.listAll()[0])

        Assert.assertEquals(new BigDecimal('14.42'), notaNegociacaoGravada.taxaLiquidacao)
        Assert.assertEquals(new BigDecimal('1.92'), notaNegociacaoGravada.emolumentos)
        Assert.assertEquals(new BigDecimal('170.10'), notaNegociacaoGravada.taxaOperacional)
        Assert.assertEquals(new BigDecimal('18.16'), notaNegociacaoGravada.impostos)
        Assert.assertEquals(new BigDecimal('1.32'), notaNegociacaoGravada.irpfVendas)
        Assert.assertEquals(new BigDecimal('6.63'), notaNegociacaoGravada.outrosCustos)

        def aper3f = tituloRepository.getByTicker('aper3f')
        def bpan4 = tituloRepository.getByTicker('bpan4')

        Assert.assertEquals(LocalDate.of(2020, 2, 3), aper3f.dataEntrada)
        Assert.assertEquals(TipoTituloEnum.a, aper3f.tipo)
        Assert.assertEquals(30, aper3f.qtde)
        Assert.assertEquals(new BigDecimal('802.14'), aper3f.valorTotalInvestido)
        Assert.assertEquals(LocalDate.of(2020, 2, 3), bpan4.dataEntrada)
        Assert.assertEquals(TipoTituloEnum.a, bpan4.tipo)
        Assert.assertEquals(300, bpan4.qtde)
        Assert.assertEquals(new BigDecimal('1215.26'), bpan4.valorTotalInvestido)
    }

}

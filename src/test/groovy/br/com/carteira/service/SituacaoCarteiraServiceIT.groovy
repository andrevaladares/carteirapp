package br.com.carteira.service

import br.com.carteira.exception.QuantidadeTituloException
import br.com.carteira.repository.SituacaoCarteiraRepository
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
@Sql(scripts = ["classpath:limpaDados.sql", "classpath:titulosSituacaoCarteira.sql"])
class SituacaoCarteiraServiceIT {

    @Autowired
    SituacaoCarteiraService situacaoCarteiraService
    @Autowired
    SituacaoCarteiraRepository situacaoCarteiraRepository

    @Test
    void 'grava corretamente a situacao de um conjunto de titulos'(){
        def nomeArquivo = 'situacaoAcoesTeste.txt'
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def dataReferencia = LocalDate.of(2020, 2, 28)

        situacaoCarteiraService.
                importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)

        def situacaoAlup11 = situacaoCarteiraRepository.getByTickerDataReferencia('alup11', LocalDate.of(2020, 2, 28))
        def situacaoBpan4 = situacaoCarteiraRepository.getByTickerDataReferencia('bpan4', LocalDate.of(2020, 2, 28))

        Assert.assertEquals(400, situacaoAlup11.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('11280.00'), situacaoAlup11.valorAtual)
        Assert.assertEquals(100, situacaoBpan4.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('9900.00'), situacaoBpan4.valorAtual)
    }

    @Test
    void 'erro se quantidade no titulo nao bater com a quantidade na situacao atual' () {
        try {
            def nomeArquivo = 'situacaoQtdeInvalida.txt'
            def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
            def dataReferencia = LocalDate.of(2020, 2, 28)

            situacaoCarteiraService.
                    importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)
            Assert.fail()
        }
        catch (QuantidadeTituloException e) {
            Assert.assertEquals("A quantidade informada no arquivo precisa ser igual à quantidade atual disponível para o título. Titulo com falha: alup11", e.getMessage())
        }
    }

}

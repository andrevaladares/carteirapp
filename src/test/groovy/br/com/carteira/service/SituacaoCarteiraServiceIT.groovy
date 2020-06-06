package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.exception.QuantidadeTituloException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.SituacaoCarteiraRepository
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
@Sql(scripts = ["classpath:limpaDados.sql", "classpath:titulosSituacaoCarteira.sql"])
class SituacaoCarteiraServiceIT {

    @Autowired
    SituacaoCarteiraService situacaoCarteiraService
    @Autowired
    SituacaoCarteiraRepository situacaoCarteiraRepository
    @Autowired
    AtivoRepository ativoRepository

    @Test
    void 'grava corretamente a situacao de um conjunto de titulos'(){
        def nomeArquivo = 'situacaoAcoesTeste.txt'
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def dataReferencia = LocalDate.of(2020, 2, 28)

        situacaoCarteiraService.
                importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)

        def alupar = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(ticker: 'alup11', tipo: TipoAtivoEnum.a), 'asc')[0]
        def smal11 = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(ticker: 'smal11', tipo: TipoAtivoEnum.fin), 'asc')[0]
        def votor = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(nome: 'VOTORANTIM FIC DE FI CAMBIAL DÓLAR', tipo: TipoAtivoEnum.fiv), 'asc')[0]
        def debLight = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(nome: 'DEB LIGHT SERVICOS DE ELETRIC - OUT/2022', tipo: TipoAtivoEnum.deb), 'asc')[0]


        def situacaoAlup11 = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(alupar.id, LocalDate.of(2020, 2, 28))
        def situacaoSmal11 = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(smal11.id, LocalDate.of(2020, 2, 28))
        def situacaoVotor = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(votor.id, LocalDate.of(2020, 2, 28))
        def situacaoDebLight = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(debLight.id, LocalDate.of(2020, 2, 28))

        Assert.assertEquals(400, situacaoAlup11.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('11280.00'), situacaoAlup11.valorAtual)
        Assert.assertEquals(170, situacaoSmal11.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('7554.80'), situacaoSmal11.valorAtual)
        Assert.assertEquals(130, situacaoVotor.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('8782.80'), situacaoVotor.valorAtual)
        Assert.assertEquals(100, situacaoDebLight.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('9900.00'), situacaoDebLight.valorAtual)
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:titulos.sql"])
    @Ignore
    void 'grava corretamente a situacao com repeticao do fundo de indice'(){
        def nomeArquivo = 'situacaoBova11Repetido.txt'
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def dataReferencia = LocalDate.of(2020, 2, 28)

        situacaoCarteiraService.
                importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)

        def bova11 = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(ticker: 'bova11', tipo: TipoAtivoEnum.fin), 'asc')[0]

        def situacaoBova11 = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(bova11.id, LocalDate.of(2020, 2, 28))

        Assert.assertEquals(-1100, situacaoBova11.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('-1700.00'), situacaoBova11.valorAtual)
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

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:titulos.sql"])
    void 'grava corretamente a situacao de ouro oz2'(){
        def nomeArquivo = 'situacaoOuroOz2.txt'
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def dataReferencia = LocalDate.of(2020, 2, 28)

        situacaoCarteiraService.
                importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)

        def oz2 = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(ticker: 'oz2', tipo: TipoAtivoEnum.oz2), 'asc')[0]

        def situacaoOz2 = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(oz2.id, LocalDate.of(2020, 2, 28))

        Assert.assertEquals(24, situacaoOz2.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('70000.00'), situacaoOz2.valorAtual)
    }

    @Test
    @Sql(scripts = ["classpath:limpaDados.sql", "classpath:ativosCriDebenture.sql"])
    void 'grava corretamente a situacao cri e debenture'(){
        def nomeArquivo = 'situacaoCriDebenture.txt'
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def dataReferencia = LocalDate.of(2020, 2, 28)

        situacaoCarteiraService.
                importarSituacaoTitulos(caminhoArquivo, nomeArquivo, dataReferencia)

        def cri = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(nome: 'CRI Direcional - ABR/2021', tipo: TipoAtivoEnum.cri), 'asc')[0]
        def deb = ativoRepository.getAllByAtivoExample(Ativo.getInstanceWithAtributeMap(nome: 'DEB LIGHT SERVICOS DE ELETRIC - OUT/2022', tipo: TipoAtivoEnum.deb), 'asc')[0]

        def situacaoCri = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(cri.id, LocalDate.of(2020, 2, 28))
        def situacaoDeb = situacaoCarteiraRepository.getByDataReferenciaIdAtivo(deb.id, LocalDate.of(2020, 2, 28))

        Assert.assertEquals(1500, situacaoCri.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('8686.28'), situacaoCri.valorAtual)
        Assert.assertEquals(1200, situacaoDeb.qtdeDisponivel)
        Assert.assertEquals(new BigDecimal('16568.29'), situacaoDeb.valorAtual)
    }

}

package br.com.carteira.service

import br.com.carteira.repository.OperacaoRepository
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
class ImportarNotaEOperacaoIT {

    @Autowired
    OperacaoService operacaoService

    @Test
    void "obtem dados da nota a partir do arquivo"(){
        def caminhoArquivo = 'c:\\projetos\\carteirApp\\src\\test\\resources'
        def nomeArquivo = 'notaOperacaoTeste.txt'

        def linhasArquivo = operacaoService.carregarArquivoNotaNegociacao(caminhoArquivo, nomeArquivo)

        Assert.assertEquals(14, linhasArquivo.size())
        Assert.assertEquals('Dados das operações', linhasArquivo[8][0])
    }

}

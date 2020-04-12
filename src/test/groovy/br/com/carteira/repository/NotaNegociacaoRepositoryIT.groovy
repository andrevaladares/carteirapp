package br.com.carteira.repository

import br.com.carteira.entity.NotaNegociacao
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
@Sql(scripts = ["classpath:limpaDados.sql"])
class NotaNegociacaoRepositoryIT {
    @Autowired
    NotaNegociacaoRepository notaNegociacaoRepository

    @Test
    void "inclui uma nota de negociacao"(){
        def notaNegociacao = new NotaNegociacao(
                taxaLiquidacao: 7.64,
                emolumentos: 0.96,
                taxaOperacional: 56.70,
                impostos: 6.05,
                irpfVendas: 2,
                outrosCustos: 2.21
        )

        def idNota = notaNegociacaoRepository.incluir(notaNegociacao)
        Assert.assertNotNull(idNota)
    }
}

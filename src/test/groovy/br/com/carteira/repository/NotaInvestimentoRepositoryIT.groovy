package br.com.carteira.repository

import br.com.carteira.entity.NotaInvestimento
import br.com.carteira.entity.RegimeResgateEnum
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.time.LocalDate

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
@Sql(scripts = ["classpath:titulos.sql", "classpath:limpaDados.sql"])
class NotaInvestimentoRepositoryIT {
    @Autowired
    NotaInvestimentoRepository notaInvestimentoRepository

    @Test
    void 'inclui corretamente uma nota de investimento'() {
        def notaInvestimento = new NotaInvestimento(
                dataMovimentacao: LocalDate.of(2020, 4, 20),
                cnpjCorretora: '05217065000107',
                nomeCorretora: 'XP',
                regimeResgate: RegimeResgateEnum.fifo
        )

        def id = notaInvestimentoRepository.incluir(notaInvestimento)

        assert id != null
    }

}

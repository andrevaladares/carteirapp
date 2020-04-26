package br.com.carteira.repository

import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoOperacaoEnum
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
@Sql(scripts = ["classpath:operacao.sql"])
class OperacaoRepositoryIT {
    @Autowired
    OperacaoRepository operacaoRepository
    @Autowired
    AtivoRepository tituloRepository

    @Test
    void testIncluirOperacaoCompra() {
        def tituloGroovyRow = tituloRepository.listAll()[0]
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: tituloRepository.fromAtivoGroovyRow(tituloGroovyRow),
                qtde: 100,
                valorTotalOperacao: BigDecimal.valueOf(1200),
                custoMedioVenda: BigDecimal.valueOf(12),
                data: LocalDate.of(2019, 12, 30)
        )

        def idOperacao = operacaoRepository.incluir(operacao)
        def operacaoCriada = operacaoRepository.getById(idOperacao)

        Assert.assertEquals('c', operacaoCriada['tipo_operacao'])
    }

    @Test
    void testIncluirOperacaoVenda() {
        def tituloGroovyRow = tituloRepository.listAll()[0]
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: tituloRepository.fromAtivoGroovyRow(tituloGroovyRow),
                qtde: 50,
                valorTotalOperacao: BigDecimal.valueOf(600),
                custoMedioVenda: BigDecimal.valueOf(12),
                data: LocalDate.of(2020, 2, 10)
        )

        def idOperacao = operacaoRepository.incluir(operacao)
        def operacaoCriada = operacaoRepository.getById(idOperacao)

        Assert.assertEquals('v', operacaoCriada['tipo_operacao'])
    }
}

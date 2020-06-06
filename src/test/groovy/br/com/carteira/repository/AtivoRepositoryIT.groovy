package br.com.carteira.repository

import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.Ativo
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
@Sql(scripts = ["classpath:limpaDados.sql", "classpath:titulos.sql"])
class AtivoRepositoryIT {
    @Autowired
    AtivoRepository ativoRepository

    @Test
    void testListAll() {
        List<Ativo> resultado = ativoRepository.listAll()
        Assert.assertEquals(3, resultado.size())
        Assert.assertEquals('visc11', resultado[0]['ticker'])
    }

    @Test
    void testIncluirTituloSucesso() {
        def titulo = new Ativo (
                ticker: 'bbas3',
                nome: 'banco do brasil',
                tipo: 'a',
                setor: 'bancos',
                qtde: 10,
                valorTotalInvestido: 10.0,
                dataEntrada: LocalDate.of(2019, 4, 23)
        )

        def idTitulo = ativoRepository.incluir(titulo)
        def tituloCriado = ativoRepository.getById(idTitulo)

        Assert.assertEquals('bbas3', tituloCriado['ticker'])
    }

    @Test
    void testAtualizarTituloSucesso() {
        def titulo = Ativo.getInstanceWithAtributeMap (
                ticker: 'visc11',
                nome: 'vinci shoppings',
                tipo: TipoAtivoEnum.fii,
                setor: 'bancos',
                qtde: 100.00000000,
                valorTotalInvestido: 1012,
                dataEntrada: LocalDate.of(2020, 3, 20)
        )

        def qtdeAtualizacoes  = ativoRepository.atualizar(titulo)
        def tituloAtualizado = ativoRepository.getByTicker('visc11')

        Assert.assertEquals(1, qtdeAtualizacoes)
        Assert.assertEquals('vinci shoppings', tituloAtualizado.nome)
        Assert.assertEquals(TipoAtivoEnum.fii, tituloAtualizado.tipo)
        Assert.assertEquals('bancos', tituloAtualizado.setor)
        Assert.assertEquals(100.00000000, tituloAtualizado.qtde)
        Assert.assertEquals(new BigDecimal('1012.00'), tituloAtualizado.valorTotalInvestido)
    }

    @Test
    void 'monta mapa datas parametros'() {
        def mapa = ativoRepository.montaMapaDatas([LocalDate.of(2019,7,15), LocalDate.of(2019, 9, 12)])
        println mapa
        println "${mapa.keySet().collect{":$it"}.join(',')}"

    }
}

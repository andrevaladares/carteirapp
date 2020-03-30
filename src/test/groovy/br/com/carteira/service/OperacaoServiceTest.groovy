package br.com.carteira.service

import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.entity.Titulo
import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.TituloRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class OperacaoServiceTest {
    @Mock
    OperacaoRepository operacaoRepositoryMock
    @Mock
    TituloRepository tituloRepositoryMock
    @InjectMocks
    OperacaoService operacaoService

    @Test
    void "atualiza operação a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def operacaoCompleta = operacaoService.complementarOperacao(operacao)

        Assert.assertEquals(BigDecimal.valueOf(17.5), operacaoCompleta.custoMedioVenda)
        Assert.assertEquals(BigDecimal.valueOf(37.5), operacaoCompleta.resultadoVenda)
    }

    @Test
    void "atualiza titulo a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(185, titulo.qtde)
        Assert.assertEquals(new BigDecimal('3237.50'), titulo.valorTotalInvestido)
    }

    @Test
    void "atualiza operação a partir de compra"() {
        Operacao operacao = obterOperacaoDeCompra()

        def operacaoCompleta = operacaoService.complementarOperacao(operacao)

        Assert.assertNull(operacaoCompleta.custoMedioVenda)
        Assert.assertNull(operacaoCompleta.resultadoVenda)
    }

    @Test
    void "atualiza titulo a partir de compra"() {
        Operacao operacao = obterOperacaoDeCompra()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(2100, titulo.qtde)
        Assert.assertEquals(BigDecimal.valueOf(31200), titulo.valorTotalInvestido)
    }

    @Test
    void "inclui nova operacao"() {
        def operacao = obterOperacaoDeCompra()
        def result = new Titulo(
                ticker: 'visc11',
                qtde: 2000,
                valorTotalInvestido: 30000
        )
        Mockito.when(tituloRepositoryMock.getByTicker(operacao.titulo.ticker)).thenReturn(result)

        operacaoService.incluir(operacao)

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacao)
        Mockito.verify(tituloRepositoryMock, Mockito.times(1)).atualizar(operacao.titulo)
    }

    @Test
    void "inclui titulo a partir da operação caso ainda não exista"() {
        def operacao = obterOperacaoDeCompra()
        Mockito.when(tituloRepositoryMock.getByTicker(operacao.titulo.ticker)).thenReturn(null)

        operacaoService.incluir(operacao)

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacao)
        Mockito.verify(tituloRepositoryMock, Mockito.times(1)).incluir(operacao.titulo)

    }

    @Test
    void "erro ao tentar incluir operacao de venda para titulo novo" () {
        def operacao = obterOperacaoDeVenda()
        Mockito.when(tituloRepositoryMock.getByTicker(operacao.titulo.ticker)).thenReturn(null)
        try {
            operacaoService.incluir(operacao)
        }
        catch (OperacaoInvalidaException e) {
            Assert.assertEquals('Se o título é novo a operação não pode ser de venda', e.getMessage())
        }
    }

    private Operacao obterOperacaoDeVenda() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                titulo: new Titulo(
                        ticker: 'visc11',
                        qtde: 200,
                        valorTotalInvestido: 3500
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeCompra() {
        Operacao operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                titulo: new Titulo(
                        ticker: 'visc11',
                        qtde: 2000,
                        valorTotalInvestido: 30000
                ),
                qtde: 100,
                valorTotalOperacao: 1200
        )
        operacao
    }
}

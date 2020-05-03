package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoOperacaoEnum
import org.junit.Assert
import org.junit.Test

class AtivoTest {

    @Test
    void "atualiza ativo a partir de venda short"() {
        Operacao operacao = obterOperacaoDeVendaShort()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-15, ativo.qtde)
        Assert.assertEquals(new BigDecimal('-300'), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra short zerando posição"() {
        Operacao operacao = obterOperacaoDeCompraShortZeraPosicao()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(0, ativo.qtde)
        Assert.assertEquals(new BigDecimal('0'), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(185, ativo.qtde)
        Assert.assertEquals(new BigDecimal('3237.5000'), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra"() {
        Operacao operacao = obterOperacaoDeCompra()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(2100, ativo.qtde)
        Assert.assertEquals(BigDecimal.valueOf(31200), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra short sem zerar posição"() {
        Operacao operacao = obterOperacaoDeCompraShortSemZerarPosicao()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-5, ativo.qtde)
        Assert.assertEquals(new BigDecimal('-133.3330'), ativo.valorTotalInvestido)
    }

    private Operacao obterOperacaoDeCompraShortSemZerarPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: -15,
                        valorTotalInvestido: -400
                ),
                qtde: 10,
                valorTotalOperacao: 150
        )
        operacao
    }

    private Operacao obterOperacaoDeCompra() {
        Operacao operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: 2000,
                        valorTotalInvestido: 30000
                ),
                qtde: 100,
                valorTotalOperacao: 1200
        )
        operacao
    }
    private Operacao obterOperacaoDeVenda() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: 200,
                        valorTotalInvestido: 3500
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeVendaShort() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: 0,
                        valorTotalInvestido: 0
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeCompraShortZeraPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: -15,
                        valorTotalInvestido: -400
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

}

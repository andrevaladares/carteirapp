package br.com.carteira.entity

import org.junit.Assert
import org.junit.Test

class AtivoTest {

    @Test
    void "atualiza ativo a partir de venda short"() {
        Operacao operacao = obterOperacaoDeVendaShort()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-15.00000000, ativo.qtde)
        Assert.assertEquals(new BigDecimal('-300'), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra short zerando posicao"() {
        Operacao operacao = obterOperacaoDeCompraShortZeraPosicao()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(0.00000000, ativo.qtde)
        Assert.assertEquals(new BigDecimal('0'), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(185.00000000, ativo.qtde)
        Assert.assertEquals(3237.5000, ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra"() {
        Operacao operacao = obterOperacaoDeCompra()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(2100.00000000, ativo.qtde)
        Assert.assertEquals(BigDecimal.valueOf(31200), ativo.valorTotalInvestido)
    }

    @Test
    void "atualiza ativo a partir de compra short sem zerar posicao"() {
        Operacao operacao = obterOperacaoDeCompraShortSemZerarPosicao()

        def ativo = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-5.00000000, ativo.qtde)
        Assert.assertEquals(-133.3330, ativo.valorTotalInvestido)
    }

    @Test
    void "instancia classe a partir do nome"() {
        def teste = Class.forName('br.com.carteira.entity.Ativo').newInstance(
                ticker: 'teste'
        )

        assert 'teste' == teste.ticker
    }

    @Test
    void "calcula corretamente custo medio do ouro oz2"(){
        Ativo ouro = Ativo.getInstanceWithAtributeMap(
                ticker: 'oz2',
                valorTotalInvestido: 1000.00,
                qtde: 10,
                tipo: TipoAtivoEnum.o
        )

        assert ouro.obterCustoMedio() == BigDecimal.valueOf(10.0100)
    }

    private Operacao obterOperacaoDeCompraShortSemZerarPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: -15.00000000,
                        valorTotalInvestido: -400
                ),
                qtde: 10.00000000,
                valorTotalOperacao: 150
        )
        operacao
    }

    private Operacao obterOperacaoDeCompra() {
        Operacao operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: 2000.00000000,
                        valorTotalInvestido: 30000
                ),
                qtde: 100.00000000,
                valorTotalOperacao: 1200
        )
        operacao
    }
    private Operacao obterOperacaoDeVenda() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: new Ativo(
                        ticker: 'visc11',
                        qtde: 200.00000000,
                        valorTotalInvestido: 3500
                ),
                qtde: 15.00000000,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeVendaShort() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: 0.00000000,
                        valorTotalInvestido: 0
                ),
                qtde: 15.00000000,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeCompraShortZeraPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: -15.00000000,
                        valorTotalInvestido: -400
                ),
                qtde: 15.00000000,
                valorTotalOperacao: 300
        )
        operacao
    }

}

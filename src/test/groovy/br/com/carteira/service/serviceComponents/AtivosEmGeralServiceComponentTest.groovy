package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class AtivosEmGeralServiceComponentTest {
    @Mock
    OperacaoRepository operacaoRepositoryMock
    @Mock
    AtivoRepository ativoRepositoryMock
    @InjectMocks
    AtivosEmGeralServiceComponent ativosEmGeralServiceComponent


    @Test
    void "atualiza operacao a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def operacoesComplementadasList = ativosEmGeralServiceComponent.complementarOperacao(operacao)

        assert operacoesComplementadasList[0].custoMedioOperacao == 17.50000000
        assert operacoesComplementadasList[0].resultadoVenda == new BigDecimal('37.50')
    }

    @Test
    void "atualiza operacao a partir de venda short"() {
        Operacao operacao = obterOperacaoDeVendaShort()

        def operacoesComplementadasList = ativosEmGeralServiceComponent.complementarOperacao(operacao)

        assert operacoesComplementadasList[0].custoMedioOperacao == 0.0
        assert operacoesComplementadasList[0].resultadoVenda == 0.0
    }

    @Test
    void "atualiza operacao a partir de compra ou reducao short"() {
        Operacao operacao = obterOperacaoDeCompraShortSemZerarPosicao()

        def operacoesComplementadasList = ativosEmGeralServiceComponent.complementarOperacao(operacao)

        assert operacoesComplementadasList[0].custoMedioOperacao == new BigDecimal('26.66666667')
        assert operacoesComplementadasList[0].resultadoVenda == new BigDecimal('116.6666667')
    }

    @Test
    void "atualiza operacao a partir de compra em remontagem de posicao (ativo existente com saldo 0)"() {
        Operacao operacao = obterOperacaoDeCompraAPartirDeAtivoComSaldoZero()

        def operacoesComplementadasList = ativosEmGeralServiceComponent.complementarOperacao(operacao)

        assert operacoesComplementadasList[0].custoMedioOperacao == 0.0
        assert operacoesComplementadasList[0].resultadoVenda == 0.0
    }

    @Test
    void "atualiza operacao a partir de compra"() {
        Operacao operacao = obterOperacaoDeCompra()

        def operacoesComplementadasList = ativosEmGeralServiceComponent.complementarOperacao(operacao)

        assert operacoesComplementadasList[0].custoMedioOperacao == 0.0
        assert operacoesComplementadasList[0].resultadoVenda == 0.0
    }

    @Test
    void "inclui nova operacao de compra"() {
        def operacao = obterOperacaoDeCompra()
        def result = Ativo.getInstanceWithAtributeMap(
                ticker: 'visc11',
                qtde: 2000,
                valorTotalInvestido: 30000
        )
        Mockito.when(ativoRepositoryMock.getByTicker(operacao.ativo.ticker)).thenReturn(result)
        Mockito.when(ativoRepositoryMock.getByTicker('brl')).thenReturn(
                Ativo.getInstanceWithAtributeMap(ticker: 'brl', qtde: 2000)
        )

        ativosEmGeralServiceComponent.incluir(operacao)

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacao)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacao.ativo)
    }

    @Test
    void "inclui nova operacao de venda"() {
        def operacao = obterOperacaoDeVenda()
        def result = Ativo.getInstanceWithAtributeMap(
                ticker: 'visc11',
                qtde: 2000,
                valorTotalInvestido: 30000
        )
        Mockito.when(ativoRepositoryMock.getByTicker(operacao.ativo.ticker)).thenReturn(result)
        Mockito.when(ativoRepositoryMock.getByTicker('brl')).thenReturn(
                Ativo.getInstanceWithAtributeMap(ticker: 'brl', qtde: 2000)
        )

        ativosEmGeralServiceComponent.incluir(operacao)

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacao)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacao.ativo)
    }

    @Test
    void "inclui titulo a partir da operacao caso ainda nao exista"() {
        def operacao = obterOperacaoDeCompra()
        Mockito.when(ativoRepositoryMock.getByTicker(operacao.ativo.ticker)).thenReturn(null)
        Mockito.when(ativoRepositoryMock.getByTicker('brl')).thenReturn(
                Ativo.getInstanceWithAtributeMap(ticker: 'brl', qtde: 2000)
        )

        ativosEmGeralServiceComponent.incluir(operacao)

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacao)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).incluir(operacao.ativo)

    }

    @Test
    void "cria operacao de short para titulo novo" () {
        def operacao = obterOperacaoDeVenda()
        def titulo = ativosEmGeralServiceComponent.criarAtivoAPartirDaOperacao(operacao)

        assert titulo.ticker == 'visc11'
        assert titulo.qtde == -15.00000000
        assert titulo.valorTotalInvestido == new BigDecimal('-300')
    }

    private Operacao obterOperacaoDeCompraAPartirDeAtivoComSaldoZero() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: 0,
                        valorTotalInvestido: -0.4
                ),
                qtde: 10,
                valorTotalOperacao: 150
        )
        operacao
    }

    private Operacao obterOperacaoDeVenda() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                ativo: Ativo.getInstanceWithAtributeMap(
                        ticker: 'visc11',
                        qtde: 200.00000000,
                        valorTotalInvestido: 3500,
                        tipo: TipoAtivoEnum.fii
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
                        qtde: 0,
                        valorTotalInvestido: 0
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeCompraShortSemZerarPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                ativo: Ativo.getInstanceWithAtributeMap(
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
                ativo: Ativo.getInstanceWithAtributeMap(
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

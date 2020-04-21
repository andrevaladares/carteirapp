package br.com.carteira.service

import br.com.carteira.entity.NotaNegociacao
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

        Assert.assertEquals(17.5000, operacaoCompleta.custoMedioVenda)
        Assert.assertEquals(BigDecimal.valueOf(37.5), operacaoCompleta.resultadoVenda)
    }

    @Test
    void "atualiza operação a partir de venda short"() {
        Operacao operacao = obterOperacaoDeVendaShort()

        def operacaoCompleta = operacaoService.complementarOperacao(operacao)

        Assert.assertNull(operacaoCompleta.custoMedioVenda)
        Assert.assertNull(operacaoCompleta.resultadoVenda)
    }

    @Test
    void "atualiza operação a partir de compra/redução short"() {
        Operacao operacao = obterOperacaoDeCompraShortSemZerarPosicao()

        def operacaoCompleta = operacaoService.complementarOperacao(operacao)

        Assert.assertEquals(new BigDecimal('26.6667'), operacaoCompleta.custoMedioVenda)
        Assert.assertEquals(new BigDecimal('116.667'), operacaoCompleta.resultadoVenda)
    }

    @Test
    void "atualiza titulo a partir de venda"() {
        Operacao operacao = obterOperacaoDeVenda()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(185, titulo.qtde)
        Assert.assertEquals(new BigDecimal('3237.5000'), titulo.valorTotalInvestido)
    }

    @Test
    void "atualiza titulo a partir de venda short"() {
        Operacao operacao = obterOperacaoDeVendaShort()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-15, titulo.qtde)
        Assert.assertEquals(new BigDecimal('-300'), titulo.valorTotalInvestido)
    }

    @Test
    void "atualiza titulo a partir de compra short zerando posição"() {
        Operacao operacao = obterOperacaoDeCompraShortZeraPosicao()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(0, titulo.qtde)
        Assert.assertEquals(new BigDecimal('0'), titulo.valorTotalInvestido)
    }

    @Test
    void "atualiza titulo a partir de compra short sem zerar posição"() {
        Operacao operacao = obterOperacaoDeCompraShortSemZerarPosicao()

        def titulo = operacaoService.atualizarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals(-5, titulo.qtde)
        Assert.assertEquals(new BigDecimal('-133.3330'), titulo.valorTotalInvestido)
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
    void "cria operação de short para título novo" () {
        def operacao = obterOperacaoDeVenda()
        def titulo = operacaoService.criarTituloAPartirDaOperacao(operacao)

        Assert.assertEquals('visc11', titulo.ticker)
        Assert.assertEquals(-15, titulo.qtde)
        Assert.assertEquals(new BigDecimal('-300'), titulo.valorTotalInvestido)
    }

    @Test
    void "obtem corretamente uma nota de negociacao a partir da lista de linhas do arquivo"(){
        ArrayList<String[]> linhasArquivo = montaArquivoNota()

        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(linhasArquivo)
        def notaNegociacaoEsperada = new NotaNegociacao(
                taxaLiquidacao: new BigDecimal('14.42'),
                emolumentos: new BigDecimal('1.92'),
                taxaOperacional: new BigDecimal('170.1'),
                impostos: new BigDecimal('18.16'),
                irpfVendas: new BigDecimal('1.32'),
                outrosCustos: new BigDecimal('6.63')
        )

        Assert.assertEquals(notaNegociacaoEsperada, notaNegociacao)
    }

    @Test
    void "calcula corretamente valor taxa unitária das operacoes de compra"() {
        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(montaArquivoNota())
        def valorUnitarioTaxas = operacaoService.defineValorTaxaUnitaria(montaArquivoNota(), notaNegociacao)

        Assert.assertEquals(new BigDecimal('0.5381'), valorUnitarioTaxas)
    }

    @Test
    void "calcula corretamente valor taxa unitária das quando so ha operacao de venda"() {
        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(montaArquivoNotaApenasVendas())
        def valorUnitarioTaxas = operacaoService.defineValorTaxaUnitaria(montaArquivoNotaApenasVendas(), notaNegociacao)

        Assert.assertEquals(new BigDecimal('0.5593'), valorUnitarioTaxas)
    }

    private ArrayList<String[]> montaArquivoNota() {
        def linhasArquivo = new ArrayList<String[]>([
                ['Dados da nota'],
                ['Data do pregão', '03/02/2020'],
                ['Taxa de liquidação', '14,42'],
                ['Emolumentos', '1,92'],
                ['Taxa operacional', '170,1'],
                ['Impostos', '18,16'],
                ['I.R.R.F. s/ operações', '1,32'],
                ['Outros', '6,63'],
                ['Dados das operações'],
                ['tipo', 'título', 'TipoTitulo', 'Preço compra', 'Quantidade', 'Valor'],
                ['c', 'Aper3f', 'a', '26,20', '40', '1047,83'],
                ['c', 'Enev3f', 'a', '18,91', '55', '1039,93'],
                ['v', 'Jpsa3f', 'a', '25,35', '40', '1013,83'],
                ['c', 'Bpan4', 'a', '3,51', '300', '1053,83']
        ])
        linhasArquivo
    }

    private ArrayList<String[]> montaArquivoNotaApenasVendas() {
        def linhasArquivo = new ArrayList<String[]>([
                ['Dados da nota'],
                ['Data do pregão', '03/02/2020'],
                ['Taxa de liquidação', '14,42'],
                ['Emolumentos', '1,92'],
                ['Taxa operacional', '170,1'],
                ['Impostos', '18,16'],
                ['I.R.R.F. s/ operações', '1,32'],
                ['Outros', '6,63'],
                ['Dados das operações'],
                ['tipo', 'título', 'TipoTitulo', 'Preço compra', 'Quantidade', 'Valor'],
                ['v', 'Aper3f', 'a', '26,20', '40', '1047,83'],
                ['v', 'Jpsa3f', 'a', '25,35', '40', '1013,83'],
                ['v', 'Bpan4', 'a', '3,51', '300', '1053,83']
        ])
        linhasArquivo
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

    private Operacao obterOperacaoDeVendaShort() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                titulo: new Titulo(
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
                titulo: new Titulo(
                        ticker: 'visc11',
                        qtde: -15,
                        valorTotalInvestido: -400
                ),
                qtde: 15,
                valorTotalOperacao: 300
        )
        operacao
    }

    private Operacao obterOperacaoDeCompraShortSemZerarPosicao() {
        def operacao = new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                titulo: new Titulo(
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

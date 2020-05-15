package br.com.carteira.service

import br.com.carteira.entity.NotaNegociacao
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.entity.Ativo
import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.AtivoRepository
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
    AtivoRepository tituloRepositoryMock
    @InjectMocks
    OperacaoService operacaoService

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
                outrosCustos: new BigDecimal('6.63'),
                taxaRegistroBmf: new BigDecimal('2.38'),
                taxasBmfEmolFgar: new BigDecimal('0.78')
        )

        Assert.assertEquals(notaNegociacaoEsperada, notaNegociacao)
    }

    @Test
    void "calcula corretamente valor taxa unitaria das operacoes de compra"() {
        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(montaArquivoNota())
        def valorUnitarioTaxas = operacaoService.defineValorTaxaUnitaria(montaArquivoNota(), notaNegociacao)

        Assert.assertEquals(new BigDecimal('0.5461'), valorUnitarioTaxas)
    }

    @Test
    void "calcula corretamente valor taxa unitaria quando so ha operacao de venda"() {
        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(montaArquivoNotaApenasVendas())
        def valorUnitarioTaxas = operacaoService.defineValorTaxaUnitaria(montaArquivoNotaApenasVendas(), notaNegociacao)

        Assert.assertEquals(new BigDecimal('0.5677'), valorUnitarioTaxas)
    }

    @Test
    void "calcula corretamente valor taxa unitaria quando so ha operacao de venda de ouro"() {
        def notaNegociacao = operacaoService.obterDadosNotaNegociacao(montaArquivoNotaApenasVendasOuro())
        def valorUnitarioTaxas = operacaoService.defineValorTaxaUnitaria(montaArquivoNotaApenasVendasOuro(), notaNegociacao)

        Assert.assertEquals(new BigDecimal('22.9900'), valorUnitarioTaxas)
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
                ['Taxa Registro BM&F', '2,38'],
                ['Taxas BM&F (emol+f.gar)',	'0,78'],
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
                ['Taxa Registro BM&F', '2,38'],
                ['Taxas BM&F (emol+f.gar)',	'0,78'],
                ['Dados das operações'],
                ['tipo', 'título', 'TipoTitulo', 'Preço compra', 'Quantidade', 'Valor'],
                ['v', 'Aper3f', 'a', '26,20', '40', '1047,83'],
                ['v', 'Jpsa3f', 'a', '25,35', '40', '1013,83'],
                ['v', 'Bpan4', 'a', '3,51', '300', '1053,83']
        ])
        linhasArquivo
    }

    private ArrayList<String[]> montaArquivoNotaApenasVendasOuro() {
        def linhasArquivo = new ArrayList<String[]>([
                ['Dados da nota'],
                ['Data do pregão', '17/04/2019'],
                ['Taxa de liquidação', '0,00'],
                ['Emolumentos', '0,00'],
                ['Taxa operacional', '40,00'],
                ['Impostos', '3,86'],
                ['I.R.R.F. s/ operações', '0,15'],
                ['Outros', '1,56'],
                ['Taxa Registro BM&F', '0,31'],
                ['Taxas BM&F (emol+f.gar)',	'0,10'],
                ['Dados das operações'],
                ['tipo', 'título', 'TipoTitulo', 'Preço compra', 'Quantidade', 'Valor'],
                ['v', 'oz2', 'o', '160,00', '2', '3196,80']
        ])
        linhasArquivo
    }

}

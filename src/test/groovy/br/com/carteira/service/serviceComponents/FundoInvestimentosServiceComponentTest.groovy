package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.NotaInvestimento
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.OperacaoComeCotasDTO
import br.com.carteira.entity.RegimeResgateEnum
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

import java.time.LocalDate

@RunWith(MockitoJUnitRunner.class)
class FundoInvestimentosServiceComponentTest {
    @Mock
    OperacaoRepository operacaoRepositoryMock
    @Mock
    AtivoRepository ativoRepositoryMock
    @InjectMocks
    FundosInvestimentosServiceComponent fundoInvestimentoServiceComponent

    @Test
    void 'toda operacao de compra (aplicacao) gera novo item no estoque de ativos'(){
        def operacao = obterOperacaoDeCompra()
        fundoInvestimentoServiceComponent.incluir(operacao)

        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).incluir(operacao.ativo)
    }

    @Test
    void 'nao se pode criar posicao short em fundo de investimento'() {
        def operacao = obterOperacaoDeVenda()
        try {
            fundoInvestimentoServiceComponent.incluir(operacao)
            Assert.fail()
        }
        catch (OperacaoInvalidaException e) {
            assert e.getMessage() == "operação inválida. Não pode haver um short de fundo de investimento. CNPJ do fundo: $operacao.ativo.cnpjFundo"
        }
    }

    @Test
    void 'nao pode vender mais que a quantidade total do ativo' () {
        def operacao = obterOperacaoDeVenda()
        def ativosRetornados = obterListaAtivos10Unidades()
        Mockito.when(ativoRepositoryMock.getAllByAtivoExample(operacao.ativo, 'asc')).thenReturn(ativosRetornados)
        try {
            fundoInvestimentoServiceComponent.incluir(operacao)
            Assert.fail()
        }
        catch (OperacaoInvalidaException e) {
            assert e.getMessage() == "não é permitido vender mais que o estoque disponível do ativo. Cnpj do Ativo: $operacao.ativo.cnpjFundo. qtde venda: $operacao.qtde. Qtde disponível: ${ativosRetornados.sum({it-> it.qtde})}"
        }
    }

    @Test
    void 'venda de fundo de investimento consumindo apenas o primeiro ativo do estoque'() {
        def operacao = obterOperacaoDeVenda()
        def ativosRetornados = obterListaAtivosRetornados()
        Mockito.when(ativoRepositoryMock.getAllByAtivoExample(operacao.ativo, 'asc')).thenReturn(ativosRetornados)

        def operacoesRetornadas = fundoInvestimentoServiceComponent.incluir(operacao)

        assert operacoesRetornadas.size() == 1
        assert operacoesRetornadas[0].qtde == 15
        assert operacoesRetornadas[0].valorTotalOperacao == 300
        assert operacoesRetornadas[0].custoMedioOperacao == 10
        assert operacoesRetornadas[0].resultadoVenda == 150
        assert operacoesRetornadas[0].ativo.qtde == 85
        assert operacoesRetornadas[0].ativo.valorTotalInvestido == 850
        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[0])
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[0].ativo)
    }

    @Test
    void 'venda de fundo de investimento consumindo os dois primeiros ativos do estoque'() {
        def operacao = obterOperacaoDeVenda150Unidades()
        def ativosRetornados = obterListaAtivosRetornados()
        Mockito.when(ativoRepositoryMock.getAllByAtivoExample(operacao.ativo, 'asc')).thenReturn(ativosRetornados)

        def operacoesRetornadas = fundoInvestimentoServiceComponent.incluir(operacao)

        assert operacoesRetornadas.size() == 2
        assert operacoesRetornadas[0].qtde == 100
        assert operacoesRetornadas[0].valorTotalOperacao == 600
        assert operacoesRetornadas[0].custoMedioOperacao == 10
        assert operacoesRetornadas[0].resultadoVenda == -400
        assert operacoesRetornadas[0].ativo.qtde == 0
        assert operacoesRetornadas[0].ativo.valorTotalInvestido == 0

        assert operacoesRetornadas[1].qtde == 50
        assert operacoesRetornadas[1].valorTotalOperacao == 300
        assert operacoesRetornadas[1].custoMedioOperacao == 12
        assert operacoesRetornadas[1].resultadoVenda == -300
        assert operacoesRetornadas[1].ativo.qtde == 0
        assert operacoesRetornadas[1].ativo.valorTotalInvestido == 0

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[0])
        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[1])
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[0].ativo)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[1].ativo)
    }
    @Test
    void 'aplicacao de come cotas ao fundo'() {
        def ativosRetornados = obterListaAtivosRetornados()
        def operacoesComeCotas = [
                new OperacaoComeCotasDTO(
                        dataAplicacao: LocalDate.of(2019, 2, 20),
                        qtdeComeCotas: 12
                ),
                new OperacaoComeCotasDTO(
                        dataAplicacao: LocalDate.of(2019, 5, 10),
                        qtdeComeCotas: 8
                ),
                new OperacaoComeCotasDTO(
                        dataAplicacao: LocalDate.of(2019, 6, 10),
                        qtdeComeCotas: 5
                )
        ]
        Mockito.when(ativoRepositoryMock.getAllByCnpjFundoDatasEntrada('05217065000107', operacoesComeCotas.dataAplicacao)).thenReturn(ativosRetornados)

        def operacoesRetornadas = fundoInvestimentoServiceComponent.incluiOperacoesComeCotas('05217065000107', LocalDate.of(2020, 5, 10), operacoesComeCotas)

        assert operacoesRetornadas.size() == 3
        assert operacoesRetornadas[0].qtde == 12
        assert operacoesRetornadas[0].valorTotalOperacao == 120
        assert operacoesRetornadas[0].custoMedioOperacao == 10
        assert operacoesRetornadas[0].resultadoVenda == 0
        assert operacoesRetornadas[0].ativo.qtde == 88
        assert operacoesRetornadas[0].ativo.valorTotalInvestido == 880

        assert operacoesRetornadas[1].qtde == 8
        assert operacoesRetornadas[1].valorTotalOperacao == 96
        assert operacoesRetornadas[1].custoMedioOperacao == 12
        assert operacoesRetornadas[1].resultadoVenda == 0
        assert operacoesRetornadas[1].ativo.qtde == 42
        assert operacoesRetornadas[1].ativo.valorTotalInvestido == 504

        assert operacoesRetornadas[2].qtde == 5
        assert operacoesRetornadas[2].valorTotalOperacao == 75
        assert operacoesRetornadas[2].custoMedioOperacao == 15
        assert operacoesRetornadas[2].resultadoVenda == 0
        assert operacoesRetornadas[2].ativo.qtde == 95
        assert operacoesRetornadas[2].ativo.valorTotalInvestido == 1425

        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[0])
        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[1])
        Mockito.verify(operacaoRepositoryMock, Mockito.times(1)).incluir(operacoesRetornadas[2])
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[0].ativo)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[1].ativo)
        Mockito.verify(ativoRepositoryMock, Mockito.times(1)).atualizar(operacoesRetornadas[2].ativo)
    }

    private List<Ativo> obterListaAtivosRetornados() {
        [
            Ativo.getInstanceWithAtributeMap(
                    qtde: 100.00000000,
                    valorTotalInvestido: 1000,
                    tipo: TipoAtivoEnum.fiv,
                    cnpjFundo: '05217065000107',
                    dataEntrada: LocalDate.of(2019, 2, 20)
            ),
            Ativo.getInstanceWithAtributeMap(
                    qtde: 50.00000000,
                    valorTotalInvestido: 600,
                    tipo: TipoAtivoEnum.fiv,
                    cnpjFundo: '05217065000107',
                    dataEntrada: LocalDate.of(2019, 5, 10)
            ),
            Ativo.getInstanceWithAtributeMap(
                    qtde: 100.00000000,
                    valorTotalInvestido: 1500,
                    tipo: TipoAtivoEnum.fiv,
                    cnpjFundo: '05217065000107',
                    dataEntrada: LocalDate.of(2019, 6, 10)
            )
        ]
    }

    private List<Ativo> obterListaAtivos10Unidades() {
        [
            Ativo.getInstanceWithAtributeMap(
                    qtde: 5.00000000,
                    valorTotalInvestido: 1000,
                    tipo: TipoAtivoEnum.fiv,
                    cnpjFundo: '05217065000107',
                    dataEntrada: LocalDate.of(2019, 2, 20)
            ),
            Ativo.getInstanceWithAtributeMap(
                    qtde: 5.00000000,
                    valorTotalInvestido: 600,
                    tipo: TipoAtivoEnum.fiv,
                    cnpjFundo: '05217065000107',
                    dataEntrada: LocalDate.of(2019, 5, 10)
            )
        ]
    }

    private Operacao obterOperacaoDeVenda() {
        new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                notaInvestimento: new NotaInvestimento(regimeResgate: RegimeResgateEnum.fifo),
                ativo: Ativo.getInstanceWithAtributeMap(
                        qtde: 200.00000000,
                        valorTotalInvestido: 3500,
                        tipo: TipoAtivoEnum.fiv,
                        cnpjFundo: '05217065000107'
                ),
                qtde: 15.00000000,
                valorTotalOperacao: 300
        )
    }

    private Operacao obterOperacaoDeVenda150Unidades() {
        new Operacao(
                tipoOperacao: TipoOperacaoEnum.v,
                notaInvestimento: new NotaInvestimento(regimeResgate: RegimeResgateEnum.fifo),
                ativo: Ativo.getInstanceWithAtributeMap(
                        qtde: 200.00000000,
                        valorTotalInvestido: 3500,
                        tipo: TipoAtivoEnum.fiv,
                        cnpjFundo: '05217065000107'
                ),
                qtde: 150.00000000,
                valorTotalOperacao: 900
        )
    }

    private Operacao obterOperacaoDeCompra() {
        new Operacao(
                tipoOperacao: TipoOperacaoEnum.c,
                notaInvestimento: new NotaInvestimento(regimeResgate: RegimeResgateEnum.fifo),
                ativo: Ativo.getInstanceWithAtributeMap(
                        qtde: 2000,
                        valorTotalInvestido: 30000,
                        tipo: TipoAtivoEnum.fiv,
                        cnpjFundo: '05217065000107'
                ),
                qtde: 100,
                valorTotalOperacao: 1200
        )
    }

}

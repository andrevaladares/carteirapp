package br.com.carteira.service


import br.com.carteira.entity.NotaNegociacao
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.OperacaoComeCotasDTO
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.Ativo
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.repository.NotaInvestimentoRepository
import br.com.carteira.repository.NotaNegociacaoRepository
import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.service.serviceComponents.AtivosEmGeralServiceComponent
import br.com.carteira.service.serviceComponents.AtivosUsComponentService
import br.com.carteira.service.serviceComponents.FundosInvestimentosServiceComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class OperacaoService {
    OperacaoRepository operacaoRepository
    AtivoRepository ativoRepository
    NotaInvestimentoRepository notaInvestimentoRepository
    FundosInvestimentosServiceComponent fundosInvestimentosComponentService
    AtivosEmGeralServiceComponent ativosEmGeralComponentService
    NotaNegociacaoRepository notaNegociacaoRepository
    AtivosUsComponentService ativosUsComponentService

    @Autowired
    OperacaoService(OperacaoRepository operacaoRepository,
                    AtivoRepository ativoRepository,
                    NotaNegociacaoRepository notaNegociacaoRepository,
                    NotaInvestimentoRepository notaInvestimentoRepository,
                    FundosInvestimentosServiceComponent fundosInvestimentosComponentService,
                    AtivosEmGeralServiceComponent ativosEmGeralComponentService) {
        this.operacaoRepository = operacaoRepository
        this.ativoRepository = ativoRepository
        this.notaNegociacaoRepository = notaNegociacaoRepository
        this.notaInvestimentoRepository = notaInvestimentoRepository
        this.fundosInvestimentosComponentService = fundosInvestimentosComponentService
        this.ativosEmGeralComponentService = ativosEmGeralComponentService
    }

    @Transactional
    void importarArquivoOperacao(String caminho, String nomeArquivo) {
        def linhaAberta
        def qtdeLinhasProcessadas = 0
        new File(caminho, nomeArquivo).eachLine { linha, numeroLinha ->
            linhaAberta = linha.split('\\t')
            incluiOperacoesAPartirArquivoOperacoes(numeroLinha, linhaAberta)
            qtdeLinhasProcessadas += 1
        }
        println "Concluido processamento de ${qtdeLinhasProcessadas - 1} linhas" // Desconta a linha de títulos
    }

    private void incluiOperacoesAPartirArquivoOperacoes(int numeroLinha, String[] linhaAberta) {
        def operacao
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')
        if (numeroLinha == 1) {
            if (linhaAberta[0] != 'Data compra')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            operacao = new Operacao(
                    data: LocalDate.parse(linhaAberta[0], dateFormatter),
                    tipoOperacao: linhaAberta[1],
                    ativo: Ativo.getInstanceWithAtributeMap(
                            ticker: linhaAberta[2],
                            tipo: linhaAberta[3].toLowerCase() as TipoAtivoEnum
                    ),
                    qtde: Integer.valueOf(linhaAberta[5]),
                    valorTotalOperacao: new BigDecimal(linhaAberta[6].replace(',', '.'))
            )

            this.ativosEmGeralComponentService.incluir(operacao)
        }
    }

    @Transactional
    void importarOperacoesNotaNegociacao(String caminhoArquivo, String nomeArquivo) {
        def linhasArquivo = new File(caminhoArquivo, nomeArquivo).collect { it -> it.split('\\t') }

        def notaNegociacao = obterDadosNotaNegociacao(linhasArquivo)
        def idNotaNegociacao = notaNegociacaoRepository.incluir(notaNegociacao)
        def dataPregao = linhasArquivo[1][1]
        println 'Iniciando processamento das operações da nota'
        def qtdeProcessada = 0
        def valorTaxaUnitaria = defineValorTaxaUnitaria(linhasArquivo, notaNegociacao)
        boolean notaContemCompras = linhasArquivo.any { it[0] == 'c' }

        linhasArquivo.subList(11, linhasArquivo.size()).eachWithIndex { linha, numeroLinha ->
            ativosEmGeralComponentService.incluiOperacao(linha, numeroLinha, idNotaNegociacao, dataPregao, valorTaxaUnitaria, notaContemCompras)
            qtdeProcessada += 1
        }
        println "Concluído o processamento de ${qtdeProcessada - 1} operações" //Desconta linha de títulos
    }

    @Transactional
    void importarOperacoesNotaNegociacaoUs(String caminhoArquivo, String nomeArquivo) {
        def linhasArquivo = new File(caminhoArquivo, nomeArquivo).collect { it -> it.split('\\t') }

        def notaNegociacao = obterDadosNotaNegociacao(linhasArquivo)
        def idNotaNegociacao = notaNegociacaoRepository.incluir(notaNegociacao)
        def dataPregao = linhasArquivo[1][1]
        println 'Iniciando processamento das operações da nota'
        def qtdeProcessada = 0

        linhasArquivo.subList(11, linhasArquivo.size()).eachWithIndex { linha, numeroLinha ->
            ativosUsComponentService.incluiOperacao(linha, numeroLinha, idNotaNegociacao, dataPregao)
            qtdeProcessada += 1
        }
        println "Concluído o processamento de ${qtdeProcessada - 1} operações" //Desconta linha de títulos
    }

    @Transactional
    void importarOperacoesNotaInvestimento(String caminhoArquivo, String nomeArquivo) {
        def linhasArquivo = new File(caminhoArquivo, nomeArquivo).collect { it -> it.split('\\t') }

        def notaInvestimento = fundosInvestimentosComponentService.obterDadosNotaInvestimento(linhasArquivo)
        notaInvestimento.id = notaInvestimentoRepository.incluir(notaInvestimento)
        def dataOperacao = notaInvestimento.dataMovimentacao
        println 'Iniciando processamento das operações da nota'

        linhasArquivo.subList(6, linhasArquivo.size()).eachWithIndex { linha, numeroLinha ->
            fundosInvestimentosComponentService.incluiOperacao(linha, numeroLinha, notaInvestimento, dataOperacao)
        }
        println "Concluído o processamento das operações"
    }

    NotaNegociacao obterDadosNotaNegociacao(List<String[]> linhasArquivoNota) {
        new NotaNegociacao(
                taxaLiquidacao: new BigDecimal(linhasArquivoNota[2][1].replace(',', '.')),
                emolumentos: new BigDecimal(linhasArquivoNota[3][1].replace(',', '.')),
                taxaOperacional: new BigDecimal(linhasArquivoNota[4][1].replace(',', '.')),
                impostos: new BigDecimal(linhasArquivoNota[5][1].replace(',', '.')),
                irpfVendas: new BigDecimal(linhasArquivoNota[6][1].replace(',', '.')),
                outrosCustos: new BigDecimal(linhasArquivoNota[7][1].replace(',', '.')),
                taxaRegistroBmf: new BigDecimal(linhasArquivoNota[8][1].replace(',', '.')),
                taxasBmfEmolFgar: new BigDecimal(linhasArquivoNota[9][1].replace(',', '.'))
        )
    }

    BigDecimal defineValorTaxaUnitaria(List<String[]> listaOperacoes, NotaNegociacao notaNegociacao) {
        def valorTotalTaxas = notaNegociacao.getTotalTaxas()
        def titulosCompra = listaOperacoes
                .findAll { it[0] == 'c' }
        def qtdeParaDivisao
        if (titulosCompra) {
            qtdeParaDivisao = titulosCompra.sum { it[4].replace(',', '.') as BigDecimal } as BigDecimal
        } else {
            //Só tem vendas
            qtdeParaDivisao = listaOperacoes
                    .findAll { it[0] == 'v' }
                    .sum { it[4] as BigDecimal } as BigDecimal
        }

        valorTotalTaxas.divide(qtdeParaDivisao, 4, RoundingMode.HALF_UP)
    }

    @Transactional
    List<Operacao> incluiOperacoesComeCotas(String cnpjFundo, LocalDate dataOperacaoComeCotas, List<OperacaoComeCotasDTO> operacaoComeCotasDTO) {
        fundosInvestimentosComponentService.incluiOperacoesComeCotas(cnpjFundo, dataOperacaoComeCotas, operacaoComeCotasDTO)
    }
}

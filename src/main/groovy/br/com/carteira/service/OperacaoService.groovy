package br.com.carteira.service

import br.com.carteira.entity.NotaNegociacao
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.entity.TipoTituloEnum
import br.com.carteira.entity.Titulo
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.repository.NotaNegociacaoRepository
import br.com.carteira.repository.OperacaoRepository
import br.com.carteira.repository.TituloRepository
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
    TituloRepository tituloRepository
    NotaNegociacaoRepository notaNegociacaoRepository

    @Autowired
    OperacaoService(OperacaoRepository operacaoRepository,
                    TituloRepository tituloRepository,
                    NotaNegociacaoRepository notaNegociacaoRepository) {
        this.operacaoRepository = operacaoRepository
        this.tituloRepository = tituloRepository
        this.notaNegociacaoRepository = notaNegociacaoRepository
    }

    /**
     * Inclui uma operação para um título
     * Junto com a nova operação, atualiza os valores de estoque do título (quantidade e valor total investido até então)
     *
     * @param operacao a operação a ser inserida
     * @return o id da operação gravada
     */
    Operacao incluir(Operacao operacao) {
        def titulo = tituloRepository.getByTicker(operacao.titulo.ticker.toLowerCase())
        def operacaoAtualizada
        if (titulo != null) {
            operacao.titulo = titulo
            operacaoAtualizada = complementarOperacao(operacao)
            Titulo tituloParaAtualizacao = atualizarTituloAPartirDaOperacao(operacao)
            tituloRepository.atualizar(tituloParaAtualizacao)
        } else {
            operacao.titulo = criarTituloAPartirDaOperacao(operacao)
            operacaoAtualizada = complementarOperacao(operacao)
            operacaoAtualizada.titulo.id = tituloRepository.incluir(operacao.titulo)
        }
        operacaoRepository.incluir(operacaoAtualizada)

        operacao
    }

    /**
     * Atualiza na operação o custo médio e o resultado da venda
     * @param operacao a operação de referência
     * @return a operação atualizada
     */
    Operacao complementarOperacao(Operacao operacao) {
        if (operacao.tipoOperacao == TipoOperacaoEnum.v && operacao.titulo.qtde > 0) {
            //operacao de venda comum (redução de posição comprada)
            operacao.custoMedioVenda = operacao.titulo.valorTotalInvestido.divide(operacao.titulo.qtde as BigDecimal, 4, RoundingMode.HALF_UP)
            operacao.resultadoVenda = operacao.valorTotalOperacao - BigDecimal.valueOf(operacao.custoMedioVenda * operacao.qtde)
        }

        if (operacao.tipoOperacao == TipoOperacaoEnum.c && operacao.titulo.qtde <= 0) {
            //Reducao de um short
            operacao.custoMedioVenda = operacao.titulo.valorTotalInvestido.divide(operacao.titulo.qtde as BigDecimal, 4, RoundingMode.HALF_UP)
            operacao.resultadoVenda = (operacao.valorTotalOperacao - BigDecimal.valueOf(operacao.custoMedioVenda * operacao.qtde)) * -1
        }

        operacao
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Titulo atualizarTituloAPartirDaOperacao(Operacao operacao) {
        def tituloParaAtualizacao = operacao.titulo
        if (tituloParaAtualizacao.qtde < 0 || (tituloParaAtualizacao.qtde == 0 && operacao.tipoOperacao.equals(TipoOperacaoEnum.v))) {
            return atualizarTituloOperacaoShort(operacao, tituloParaAtualizacao)
        } else {
            return atualizarTituloOperacaoComum(operacao, tituloParaAtualizacao)
        }
    }

    private Titulo atualizarTituloOperacaoComum(Operacao operacao, Titulo tituloParaAtualizacao) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            def valorInvestidoEquivalente = tituloParaAtualizacao
                    .valorTotalInvestido.divide(
                    BigDecimal.valueOf(tituloParaAtualizacao.qtde), 4, RoundingMode.HALF_UP) * operacao.qtde
            tituloParaAtualizacao.qtde -= operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido -= valorInvestidoEquivalente

        } else {
            tituloParaAtualizacao.qtde += operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido += operacao.valorTotalOperacao
        }

        return tituloParaAtualizacao
    }

    /**
     * cria um titulo novo a partir de uma operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Titulo criarTituloAPartirDaOperacao(Operacao operacao) {
        def tituloParaAtualizacao = operacao.titulo
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            tituloParaAtualizacao.qtde = operacao.qtde * -1
            tituloParaAtualizacao.valorTotalInvestido = operacao.valorTotalOperacao * -1
            tituloParaAtualizacao.dataEntrada = operacao.data
        } else {
            tituloParaAtualizacao.qtde = operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido = operacao.valorTotalOperacao
            tituloParaAtualizacao.dataEntrada = operacao.data
        }

        tituloParaAtualizacao
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
                    titulo: new Titulo(
                            ticker: linhaAberta[2],
                            tipo: linhaAberta[3].toLowerCase() as TipoTituloEnum
                    ),
                    qtde: Integer.valueOf(linhaAberta[5]),
                    valorTotalOperacao: new BigDecimal(linhaAberta[6].replace(',', '.'))
            )

            this.incluir(operacao)
        }
    }

    @Transactional
    void importarArquivoNotaNegociacao(String caminhoArquivo, String nomeArquivo) {
        def linhasArquivo = new File(caminhoArquivo, nomeArquivo).collect { it -> it.split('\\t') }

        def notaNegociacao = obterDadosNotaNegociacao(linhasArquivo)
        def idNotaNegociacao = notaNegociacaoRepository.incluir(notaNegociacao)
        def dataPregao = linhasArquivo[1][1]
        println 'Iniciando processamento das operações da nota'
        def qtdeProcessada = 0
        def valorTaxaUnitaria = defineValorTaxaUnitaria(linhasArquivo, notaNegociacao)
        boolean notaContemCompras = linhasArquivo.any { it[0] == 'c' }

        linhasArquivo.subList(9, linhasArquivo.size()).eachWithIndex { linha, numeroLinha ->
            incluiOperacaoAPartirDeNotaNegociacao(linha, numeroLinha, idNotaNegociacao, dataPregao, valorTaxaUnitaria, notaContemCompras)
            qtdeProcessada += 1
        }
        println "Concluído o processamento de ${qtdeProcessada - 1} operações" //Desconta linha de títulos
    }

    void incluiOperacaoAPartirDeNotaNegociacao(String[] linhaAberta, Integer numeroLinha, Long idNotaNegociacao, String dataNegociacao, BigDecimal valorTaxaUnitaria, boolean notaContemCompras) {
        def operacao
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')
        if (numeroLinha == 0) {
            if (linhaAberta[0] != 'tipo')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            def valorTotalOperacao = defineValorTotalOperacao(linhaAberta, valorTaxaUnitaria, notaContemCompras)
            operacao = new Operacao(
                    data: LocalDate.parse(dataNegociacao, dateFormatter),
                    idNotaNegociacao: idNotaNegociacao,
                    tipoOperacao: linhaAberta[0],
                    titulo: new Titulo(
                            ticker: linhaAberta[1],
                            tipo: linhaAberta[2].toLowerCase() as TipoTituloEnum
                    ),
                    qtde: Integer.valueOf(linhaAberta[4]),
                    valorTotalOperacao: valorTotalOperacao
            )

            this.incluir(operacao)
        }
    }

    NotaNegociacao obterDadosNotaNegociacao(List<String[]> linhasArquivoNota) {
        new NotaNegociacao(
                taxaLiquidacao: new BigDecimal(linhasArquivoNota[2][1].replace(',', '.')),
                emolumentos: new BigDecimal(linhasArquivoNota[3][1].replace(',', '.')),
                taxaOperacional: new BigDecimal(linhasArquivoNota[4][1].replace(',', '.')),
                impostos: new BigDecimal(linhasArquivoNota[5][1].replace(',', '.')),
                irpfVendas: new BigDecimal(linhasArquivoNota[6][1].replace(',', '.')),
                outrosCustos: new BigDecimal(linhasArquivoNota[7][1].replace(',', '.'))
        )
    }

    BigDecimal defineValorTaxaUnitaria(List<String[]> listaOperacoes, NotaNegociacao notaNegociacao) {
        def valorTotalTaxas = notaNegociacao.getTotalTaxas()
        def titulosCompra = listaOperacoes
                .findAll { it[0] == 'c' }
        def qtdeParaDivisao
        if (titulosCompra) {
            qtdeParaDivisao = titulosCompra.sum { it[4] as BigDecimal } as BigDecimal
        } else {
            //Só tem vendas
            qtdeParaDivisao = listaOperacoes
                    .findAll { it[0] == 'v' }
                    .sum { it[4] as BigDecimal } as BigDecimal
        }

        valorTotalTaxas.divide(qtdeParaDivisao, 4, RoundingMode.HALF_UP)
    }

    Titulo atualizarTituloOperacaoShort(Operacao operacao, Titulo titulo) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            titulo.qtde -= operacao.qtde
            titulo.valorTotalInvestido -= operacao.valorTotalOperacao

        } else {
            def valorMedioShort = titulo.valorTotalInvestido.divide(titulo.qtde as BigDecimal, 4, RoundingMode.HALF_UP)
            titulo.qtde += operacao.qtde
            if (titulo.qtde == 0) {
                titulo.valorTotalInvestido = 0
            } else {
                def valorAAbater = valorMedioShort * operacao.qtde
                titulo.valorTotalInvestido += valorAAbater
            }
        }

        return titulo
    }

    BigDecimal defineValorTotalOperacao(String[] linhaAberta, BigDecimal valorUnitarioTaxas, boolean notaPossuiCompras) {
        def valorTotalOperacao
        def quantidade = Integer.valueOf(linhaAberta[4])
        if (linhaAberta[0] == 'c' || !notaPossuiCompras) {
            //compra sempre adiciona taxas ao valor da operação. Venda em nota que não possui compras também
            valorTotalOperacao = new BigDecimal(linhaAberta[5].replace(',', '.')).add(valorUnitarioTaxas * (quantidade as BigDecimal))
        }
        else {
            //venda em nota que possui compras não adiciona as taxas ao valor total da operação
            valorTotalOperacao = new BigDecimal(linhaAberta[5].replace(',', '.'))
        }
        valorTotalOperacao
    }
}

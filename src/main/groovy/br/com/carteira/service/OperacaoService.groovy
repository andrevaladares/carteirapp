package br.com.carteira.service

import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.entity.TipoTituloEnum
import br.com.carteira.entity.Titulo
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException
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

    @Autowired
    OperacaoService(OperacaoRepository operacaoRepository, TituloRepository tituloRepository) {
        this.operacaoRepository = operacaoRepository
        this.tituloRepository = tituloRepository
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
        if(titulo != null){
            operacao.titulo = titulo
            operacaoAtualizada = complementarOperacao(operacao)
            Titulo tituloParaAtualizacao = atualizarTituloAPartirDaOperacao(operacao)
            tituloRepository.atualizar(tituloParaAtualizacao)
        }
        else {
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
        if(operacao.tipoOperacao == TipoOperacaoEnum.v){
            operacao.custoMedioVenda = operacao.titulo.valorTotalInvestido / operacao.titulo.qtde
            operacao.resultadoVenda = operacao.valorTotalOperacao - BigDecimal.valueOf(operacao.custoMedioVenda * operacao.qtde)
        }

        operacao
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     * @param operacao  a operação de referência
     * @return o titulo atualizado
     */
    Titulo atualizarTituloAPartirDaOperacao(Operacao operacao) {
        def tituloParaAtualizacao = operacao.titulo
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            def valorInvestidoEquivalente = tituloParaAtualizacao
                    .valorTotalInvestido.divide(
                    BigDecimal.valueOf(tituloParaAtualizacao.qtde), 2, RoundingMode.HALF_UP) * operacao.qtde
            valorInvestidoEquivalente = valorInvestidoEquivalente.setScale(2, RoundingMode.HALF_UP)
            tituloParaAtualizacao.qtde -= operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido -= valorInvestidoEquivalente

        } else {
            tituloParaAtualizacao.qtde += operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido += operacao.valorTotalOperacao
        }

        tituloParaAtualizacao
    }

    /**
     * cria um titulo novo a partir de uma operação
     *
     * @param operacao  a operação de referência
     * @return o titulo atualizado
     */
    Titulo criarTituloAPartirDaOperacao(Operacao operacao) {
        def tituloParaAtualizacao = operacao.titulo
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            throw new OperacaoInvalidaException('Se o título é novo a operação não pode ser de venda')
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
        def operacao
        def qtdeLinhasProcessadas = 0
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')
        new File(caminho, nomeArquivo).eachLine { linha, numeroLinha ->
            linhaAberta = linha.split('\\t')
            if(numeroLinha == 1) {
                if(linhaAberta[0] != 'Data compra')
                    throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
            }
            else {
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
                qtdeLinhasProcessadas+=1
            }
        }
        println "Concluido processamento de ${qtdeLinhasProcessadas} linhas"
    }

}

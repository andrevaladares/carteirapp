package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait ComponentServiceTrait {

    void incluiOperacao(String[] linhaAberta, Integer numeroLinha, Long idNotaNegociacao, String dataNegociacao, BigDecimal valorTaxaUnitaria, boolean notaContemCompras) {
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
                    ativo: Ativo.getInstanceWithAtributeMap(
                            ticker: linhaAberta[1],
                            tipo: linhaAberta[2].toLowerCase() as TipoAtivoEnum
                    ),
                    qtde: new BigDecimal(linhaAberta[4].replace(',', '.')),
                    valorTotalOperacao: valorTotalOperacao
            )

            incluir(operacao)
        }
    }

    /**
     * Inclui uma operação para um título
     * Junto com a nova operação, atualiza os valores de estoque do título (quantidade e valor total investido até então)
     *
     * @param operacao a operação a ser inserida
     * @return o id da operação gravada
     */
    abstract List<Operacao> incluir(Operacao operacao)

    /**
     * Atualiza na operação o custo médio e o resultado da venda
     *
     * @param operacao a operação de referência
     * @return a operação atualizada
     */
    List<Operacao> complementarOperacao(Operacao operacao) {
        if (operacao.tipoOperacao == TipoOperacaoEnum.v && operacao.ativo.qtde > 0) {
            //operacao de venda comum (redução de posição comprada)
            operacao.custoMedioOperacao = operacao.ativo.obterCustoMedioUnitario()
            operacao.resultadoVenda = operacao.ativo.obterResultadoVenda(operacao.custoMedioOperacao, operacao.valorTotalOperacao, operacao.qtde)
        }
        else if (operacao.tipoOperacao == TipoOperacaoEnum.c && operacao.ativo.qtde < 0) {
            //Reducao de um short
            operacao.custoMedioOperacao = operacao.ativo.obterCustoMedioUnitario()
            operacao.resultadoVenda = (operacao.valorTotalOperacao - BigDecimal.valueOf(operacao.custoMedioOperacao * operacao.qtde)) * -1
        }

        [operacao]
    }

    /**
     * cria um titulo novo a partir de uma operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Ativo criarAtivoAPartirDaOperacao(Operacao operacao) {
        def tituloParaAtualizacao = operacao.ativo
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            tituloParaAtualizacao.qtde = operacao.qtde * -1
            tituloParaAtualizacao.valorTotalInvestido = operacao.valorTotalOperacao * -1
            tituloParaAtualizacao.valorInvestidoDolares = operacao.valorOperacaoDolares * -1
            tituloParaAtualizacao.dataEntrada = operacao.data
        } else {
            tituloParaAtualizacao.qtde = operacao.qtde
            tituloParaAtualizacao.valorTotalInvestido = operacao.valorTotalOperacao
            tituloParaAtualizacao.valorInvestidoDolares = operacao.valorOperacaoDolares
            tituloParaAtualizacao.dataEntrada = operacao.data
        }

        tituloParaAtualizacao
    }

    BigDecimal defineValorTotalOperacao(String[] linhaAberta, BigDecimal valorUnitarioTaxas, boolean notaPossuiCompras) {
        def valorTotalOperacao
        def quantidade = new BigDecimal(linhaAberta[4].replace(',', '.'))
        if (linhaAberta[0] == 'c') {
            //compra sempre adiciona taxas ao valor da operação
            valorTotalOperacao = new BigDecimal(linhaAberta[5].replace(',', '.')).add(valorUnitarioTaxas * (quantidade as BigDecimal))
        }
        else if (!notaPossuiCompras) {
            //Nota que só possui venda tem as taxas subtraídas dos valores de venda
            valorTotalOperacao = new BigDecimal(linhaAberta[5].replace(',', '.')) - valorUnitarioTaxas * (quantidade as BigDecimal)

        }
        else {
            //venda em nota que possui compras não adiciona as taxas ao valor total da operação
            valorTotalOperacao = new BigDecimal(linhaAberta[5].replace(',', '.'))
        }
        valorTotalOperacao
    }

    Operacao gerarTransferenciasReais(Operacao operacao) {
        def ativoReais = ativoRepository.getByTicker('brl')
        def operacaoReais = new Operacao(
                data: operacao.data,
                ativo: ativoReais,
                ativoGerador: operacao.ativo,
                qtde: operacao.valorTotalOperacao,
                valorTotalOperacao: operacao.valorTotalOperacao,
        )

        if(operacao.tipoOperacao == TipoOperacaoEnum.c) {
            operacaoReais.tipoOperacao = TipoOperacaoEnum.ts
        }
        else if (operacao.tipoOperacao == TipoOperacaoEnum.v) {
            operacaoReais.tipoOperacao = TipoOperacaoEnum.te
        }
        else {
            throw new OperacaoInvalidaException('Ativo comum deve sofrer apenas operação de compra ou venda')
        }
        operacaoReais
    }

}
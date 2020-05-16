package br.com.carteira.entity

import br.com.carteira.exception.AtivoInvalidoException

import java.math.RoundingMode

trait OperacoesAtivo {

    def validar(Ativo ativo) {
        if(ativo.ticker == null) {
            throw new AtivoInvalidoException("é obrigatório informar o ticker para ativos do tipo: $ativo.tipo")
        }
    }

    BigDecimal obterCustoMedioUnitario(BigDecimal valorTotalInvestido, BigDecimal qtde) {
        valorTotalInvestido.divide(qtde as BigDecimal, 8, RoundingMode.HALF_UP)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, BigDecimal qtde) {
        (valorTotalOperacao - custoMedioVenda * qtde).setScale(2, RoundingMode.HALF_UP)
    }

    Map atualizarAtivoOperacaoShort(Operacao operacao, BigDecimal qtde, BigDecimal valorTotalInvestido) {
        if (TipoOperacaoEnum.v == operacao.tipoOperacao) {
            qtde -= operacao.qtde
            valorTotalInvestido -= operacao.valorTotalOperacao

        } else {
            def valorMedioShort = valorTotalInvestido.divide(qtde, 4, RoundingMode.HALF_UP)
            qtde += operacao.qtde
            if (qtde == 0) {
                valorTotalInvestido = 0
            } else {
                def valorAAbater = (valorMedioShort * operacao.qtde).setScale(4, RoundingMode.HALF_UP)
                valorTotalInvestido += valorAAbater
            }
        }

        [qtde: qtde, valorTotalInvestido: valorTotalInvestido]
    }

    GString obterQueryUpdate(Ativo ativo) {
        """
            update ativo set nome = $ativo.nome, tipo = ${ativo.tipo as String},
            setor = $ativo.setor, qtde = $ativo.qtde, valor_total_investido = $ativo.valorTotalInvestido,
            data_entrada = $ativo.dataEntrada, cnpj_fundo = $ativo.cnpjFundo where ticker = $ativo.ticker 
        """
    }
}
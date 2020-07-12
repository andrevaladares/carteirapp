package br.com.carteira.entity

import java.math.RoundingMode
import java.time.LocalDate

class Ativo {
    Long id
    String ticker
    String nome
    TipoAtivoEnum tipo
    String setor
    BigDecimal qtde = 0.0
    BigDecimal valorTotalInvestido = 0.0
    BigDecimal valorInvestidoDolares = 0.0
    LocalDate dataEntrada
    String cnpjFundo
    OperacoesAtivo operacoesAtivo

    private Ativo() {
    }

    BigDecimal obterCustoMedioUnitario() {
        operacoesAtivo.obterCustoMedioUnitario(valorTotalInvestido, qtde)
    }

    BigDecimal obterResultadoVenda(BigDecimal custoMedioVenda, BigDecimal valorTotalOperacao, BigDecimal qtde) {
        operacoesAtivo.obterResultadoVenda(custoMedioVenda, valorTotalOperacao, qtde)
    }

    /**
     * Atualiza no título o valor total investido e a nova quantidade com base na operação
     *
     * @param operacao a operação de referência
     * @return o titulo atualizado
     */
    Ativo atualizarAtivoAPartirDaOperacao(Operacao operacao) {
        def retornoAtualizacao
        if (qtde < 0 || (qtde == 0 && operacao.tipoOperacao == TipoOperacaoEnum.v)) {
            retornoAtualizacao = operacoesAtivo.atualizarAtivoOperacaoShort(operacao, this.qtde, this.valorTotalInvestido)
        } else {
            retornoAtualizacao = atualizarAtivoOperacaoComum(operacao)
        }
        this.qtde = retornoAtualizacao.qtde
        this.valorTotalInvestido = retornoAtualizacao.valorTotalInvestido
        this.valorInvestidoDolares = retornoAtualizacao.valorInvestidoDolares

        this
    }

    Ativo atualizarAtivoOperacaoComum(Operacao operacao) {
        if (operacao.tipoOperacao in [TipoOperacaoEnum.v, TipoOperacaoEnum.ts]) {
            def valorInvestidoEquivalente = (valorTotalInvestido.divide(qtde, 8, RoundingMode.HALF_UP) * operacao.qtde).setScale(8, RoundingMode.HALF_UP)
            def valorInvestidoEquivalenteDolares = valorInvestidoDolares.divide(qtde, 8, RoundingMode.HALF_UP) * operacao.qtde
            qtde -= operacao.qtde
            valorTotalInvestido -= valorInvestidoEquivalente
            valorInvestidoDolares -= valorInvestidoEquivalenteDolares

        } else {
            //Aqui pode ser um compra ou transferência de entrada
            qtde += operacao.qtde
            valorTotalInvestido += operacao.valorTotalOperacao
            valorInvestidoDolares += operacao.valorOperacaoDolares
        }

        return this
    }

    static Ativo getInstance() {
        new Ativo(operacoesAtivo: new OperacoesAtivoComum())
    }

    static Ativo getInstanceWithAtributeMap(Map atributes) {
        def ativo = new Ativo(atributes)
        if(ativo.tipo == null)
            ativo.tipo = TipoAtivoEnum.a
        ativo.operacoesAtivo = Class.forName(ativo.tipo.classeOperacao).newInstance()

        ativo
    }

    void validar() {
        operacoesAtivo.validar(this)
    }

    GString obterQueryUpdate() {
        operacoesAtivo.obterQueryUpdate(this)
    }

    BigDecimal obterCustoMedioUnitarioDolares() {
        this.valorInvestidoDolares / this.qtde

    }
}
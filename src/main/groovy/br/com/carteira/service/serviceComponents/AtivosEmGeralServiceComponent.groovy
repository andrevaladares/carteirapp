package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDate

@Component
class AtivosEmGeralServiceComponent implements ComponentServiceTrait{

    OperacaoRepository operacaoRepository
    AtivoRepository ativoRepository

    @Autowired
    AtivosEmGeralServiceComponent(OperacaoRepository operacaoRepository, AtivoRepository ativoRepository) {
        this.operacaoRepository = operacaoRepository
        this.ativoRepository = ativoRepository
    }

    List<Operacao> incluir(Operacao operacao) {
        def ativo = ativoRepository.getByTicker(operacao.ativo.ticker.toLowerCase())
        def operacaoReais = gerarTransferenciasReais(operacao)
        def operacoesAtualizadasAtivo
        if (ativo != null) {
            operacao.ativo = ativo
            operacoesAtualizadasAtivo = complementarOperacao(operacao)
            operacoesAtualizadasAtivo << operacaoReais

            operacoesAtualizadasAtivo.each {
                operacaoRepository.incluir(it)
                Ativo tituloParaAtualizacao = it.ativo.atualizarAtivoAPartirDaOperacao(it)
                ativoRepository.atualizar(tituloParaAtualizacao)
            }
        } else {
            operacao.ativo = criarAtivoAPartirDaOperacao(operacao)
            operacoesAtualizadasAtivo = complementarOperacao(operacao)
            //Para ativos em geral uma operação de venda ou compra não se desdobra em mais de uma
            operacoesAtualizadasAtivo[0].ativo.id = ativoRepository.incluir(operacao.ativo)
            def ativoReais = operacaoReais.ativo.atualizarAtivoAPartirDaOperacao(operacaoReais)
            ativoRepository.atualizar(ativoReais)
            operacaoRepository.incluir(operacoesAtualizadasAtivo[0])
            operacaoRepository.incluir(operacaoReais)
        }
        operacoesAtualizadasAtivo
    }

    void movimentarRecurso(String tickerMoedaFoco, LocalDate dataDividendo, String tickerAtivoGerador,
                           BigDecimal valorMovimentacao, TipoOperacaoEnum tipoOperacao) {
        def ativoGerador = ativoRepository.getByTicker(tickerAtivoGerador)
        def ativoMoedaFoco = ativoRepository.getByTicker(tickerMoedaFoco)

        if(!ativoGerador) {
            throw new OperacaoInvalidaException("""Erro ao tentar realizar operação de dividendo para ativo inexistente.
             Ativo: $tickerAtivoGerador
            """)
        }
        def operacaoDividendo = new Operacao(
                ativo: ativoMoedaFoco,
                ativoGerador: ativoGerador,
                data: dataDividendo,
                qtde: valorMovimentacao,
                tipoOperacao: tipoOperacao,
                custoMedioOperacao: 0,
                custoMedioDolares: 0,
                valorOperacaoDolares: 0,
                valorTotalOperacao: valorMovimentacao,
                resultadoVenda: 0,
                resultadoVendaDolares: 0
        )
        operacaoRepository.incluir(operacaoDividendo)
        //Adiciona os valores recebidos na moeda informada
        if(tipoOperacao == TipoOperacaoEnum.div) {
            ativoMoedaFoco.qtde+=valorMovimentacao
            ativoMoedaFoco.valorTotalInvestido+=valorMovimentacao
        }
        else if (tipoOperacao == TipoOperacaoEnum.tx) {
            ativoMoedaFoco.qtde-=valorMovimentacao
            ativoMoedaFoco.valorTotalInvestido-=valorMovimentacao
        }
        else {
            throw new OperacaoInvalidaException('Movimentação de recursos precisa ser por dividento (div) ou taxas (tx)')
        }
        ativoRepository.atualizar(ativoMoedaFoco)
    }

    void incluirJuroTesouro(LocalDate data, String nomeTituloTesouro, BigDecimal valor) {
        def tituloGerador = ativoRepository.getByNome(nomeTituloTesouro)
        def ativoMoedaFoco = ativoRepository.getByTicker('brl')

        if(!tituloGerador || !TipoAtivoEnum.getTesouro().contains(tituloGerador.getTipo())) {
            throw new OperacaoInvalidaException("""Erro ao tentar realizar operação de juros para ativo invalido.
             Ativo: $nomeTituloTesouro
            """)
        }
        def operacao = new Operacao(
                ativo: ativoMoedaFoco,
                ativoGerador: tituloGerador,
                data: data,
                qtde: valor,
                tipoOperacao: TipoOperacaoEnum.j,
                custoMedioOperacao: 0,
                custoMedioDolares: 0,
                valorOperacaoDolares: 0,
                valorTotalOperacao: 0,
                resultadoVenda: 0,
                resultadoVendaDolares: 0
        )
        operacaoRepository.incluir(operacao)
        //Adiciona os valores recebidos na moeda informada
        ativoMoedaFoco.qtde+=valor
        ativoRepository.atualizar(ativoMoedaFoco)
    }

}

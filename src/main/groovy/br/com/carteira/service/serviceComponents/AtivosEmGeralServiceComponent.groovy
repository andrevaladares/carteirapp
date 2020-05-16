package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
        def operacoesAtualizadas
        if (ativo != null) {
            operacao.ativo = ativo
            operacoesAtualizadas = complementarOperacao(operacao)
            Ativo tituloParaAtualizacao = operacao.ativo.atualizarAtivoAPartirDaOperacao(operacao)
            ativoRepository.atualizar(tituloParaAtualizacao)
        } else {
            operacao.ativo = criarAtivoAPartirDaOperacao(operacao)
            operacoesAtualizadas = complementarOperacao(operacao)
            //Para ativos em geral uma operação de venda ou compra não se desdobra em mais de uma
            operacoesAtualizadas[0].ativo.id = ativoRepository.incluir(operacao.ativo)
        }
        operacaoRepository.incluir(operacoesAtualizadas[0])

        [operacao]
    }
}

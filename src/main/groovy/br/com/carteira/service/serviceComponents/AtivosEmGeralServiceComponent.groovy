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

    Operacao incluir(Operacao operacao) {
        def ativo = ativoRepository.getByTicker(operacao.ativo.ticker.toLowerCase())
        def operacaoAtualizada
        if (ativo != null) {
            operacao.ativo = ativo
            operacaoAtualizada = complementarOperacao(operacao)
            Ativo tituloParaAtualizacao = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)
            ativoRepository.atualizar(tituloParaAtualizacao)
        } else {
            operacao.ativo = criarAtivoAPartirDaOperacao(operacao)
            operacaoAtualizada = complementarOperacao(operacao)
            operacaoAtualizada.ativo.id = ativoRepository.incluir(operacao.ativo)
        }
        operacaoRepository.incluir(operacaoAtualizada)

        operacao
    }
}

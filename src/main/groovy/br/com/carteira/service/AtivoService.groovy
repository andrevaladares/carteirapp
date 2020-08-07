package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.repository.AtivoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AtivoService {

    AtivoRepository ativoRepository

    @Autowired
    AtivoService(AtivoRepository ativoRepository) {
        this.ativoRepository = ativoRepository
    }

    Long incluir(Ativo ativo) {
        ativo.validar()
        return ativoRepository.incluir(ativo)
    }

    @Transactional
    void atribuirBook(String nomeBook, TipoAtivoEnum tipoDoAtivo, String identificadorAtivo) {
        def ativosAAtualizar = ativoRepository.getAllByIdentificadorTipoComSaldo(tipoDoAtivo as String, identificadorAtivo)
        if(!ativosAAtualizar) {
            println "Nenhum ativo com o tipo: $tipoDoAtivo e identificador: $identificadorAtivo foi encontrado"
        }
        ativosAAtualizar.each {
            it.book = nomeBook
            ativoRepository.atualizar(it)
            println("Ativo $it.ticker/$it.nome/$it.cnpjFundo foi adicionado ao book: $nomeBook")
        }
    }
}

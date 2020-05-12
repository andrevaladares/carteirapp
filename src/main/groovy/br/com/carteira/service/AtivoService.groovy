package br.com.carteira.service

import br.com.carteira.entity.Ativo
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
}

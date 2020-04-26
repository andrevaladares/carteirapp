package br.com.carteira.service


import br.com.carteira.repository.AtivoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TituloService {

    AtivoRepository tituloRepository

    @Autowired
    TituloService(AtivoRepository tituloRepository) {
        this.tituloRepository = tituloRepository
    }

    Long incluir(titulo) {
        return tituloRepository.incluir(titulo)
    }
}

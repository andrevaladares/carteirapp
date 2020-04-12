package br.com.carteira.service


import br.com.carteira.repository.TituloRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TituloService {

    TituloRepository tituloRepository

    @Autowired
    TituloService(TituloRepository tituloRepository) {
        this.tituloRepository = tituloRepository
    }

    Long incluir(titulo) {
        return tituloRepository.incluir(titulo)
    }
}

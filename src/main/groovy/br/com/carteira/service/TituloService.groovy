package br.com.carteira.service


import br.com.carteira.repository.TituloRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
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

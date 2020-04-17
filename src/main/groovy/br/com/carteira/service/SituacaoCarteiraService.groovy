package br.com.carteira.service

import br.com.carteira.entity.SituacaoCarteira
import br.com.carteira.repository.SituacaoCarteiraRepository
import br.com.carteira.repository.TituloRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class SituacaoCarteiraService {

    TituloRepository tituloRepository
    SituacaoCarteiraRepository situacaoCarteiraRepository

    @Autowired
    SituacaoCarteiraService(TituloRepository tituloRepository,
                            SituacaoCarteiraRepository situacaoCarteiraRepository) {
        this.tituloRepository = tituloRepository
        this.situacaoCarteiraRepository = situacaoCarteiraRepository
    }

    @Transactional
    void importarSituacaoTitulos(String caminhoArquivo, String nomeArquivo, LocalDate dataReferencia) {
        List<String[]> arquivoAberto = new File(caminhoArquivo, nomeArquivo)
                .collect {it -> it.split('\\t')}
                .findAll {it.length == 5 && it[0] != 'Papel'}
                .each {it ->
                    SituacaoCarteira situacaoCarteira = montaSituacao(it, dataReferencia)
                    incluir(situacaoCarteira)
                }
    }

    SituacaoCarteira montaSituacao(String[] linhaArquivo, LocalDate dataReferencia) {
        def titulo = tituloRepository.getByTicker(linhaArquivo[0])
        new SituacaoCarteira(
                data: dataReferencia,
                idTitulo: titulo.id,
                qtdeDisponivel: Integer.valueOf(linhaArquivo[2]),
                valorInvestido: titulo.valorTotalInvestido,
                valorAtual: new BigDecimal(linhaArquivo[4].replace(',', '.'))
        )
    }

    Long incluir(SituacaoCarteira situacaoCarteira) {
        situacaoCarteiraRepository.incluir(situacaoCarteira)
    }
}

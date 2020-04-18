package br.com.carteira.service

import br.com.carteira.entity.SituacaoCarteira
import br.com.carteira.exception.QuantidadeTituloException
import br.com.carteira.repository.SituacaoCarteiraRepository
import br.com.carteira.repository.TituloRepository
import groovy.sql.GroovyRowResult
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
        new File(caminhoArquivo, nomeArquivo)
                .collect {it -> it.split('\\t')}
                .findAll {it.length == 5 && it[0] != 'Papel'}
                .each {it ->
                    SituacaoCarteira situacaoCarteira = montaSituacao(it, dataReferencia)
                    incluir(situacaoCarteira)
                }
    }

    SituacaoCarteira montaSituacao(String[] linhaArquivo, LocalDate dataReferencia) {
        def titulo = tituloRepository.getByTicker(linhaArquivo[0])
        if(titulo == null) {
            throw new QuantidadeTituloException("O ativo ${linhaArquivo[0]} não foi encontrado")
        }
        def quantidadeInformadaEmCarteira = Integer.valueOf(linhaArquivo[2])
        if(titulo.qtde != quantidadeInformadaEmCarteira) {
            throw new QuantidadeTituloException("A quantidade informada no arquivo precisa ser igual à quantidade atual disponível para o título. Titulo com falha: $titulo.ticker")
        }
        new SituacaoCarteira(
                data: dataReferencia,
                idTitulo: titulo.id,
                qtdeDisponivel: quantidadeInformadaEmCarteira,
                valorInvestido: titulo.valorTotalInvestido,
                valorAtual: new BigDecimal(linhaArquivo[4].replace(',', '.'))
        )
    }

    Long incluir(SituacaoCarteira situacaoCarteira) {
        situacaoCarteiraRepository.incluir(situacaoCarteira)
    }

    void geraSituacaoCarteira(String caminho, LocalDate dataReferencia) {
        def situacaoCarteira = situacaoCarteiraRepository.listaTodosPorDataReferencia(dataReferencia)
        def valorTotalAtual = situacaoCarteira.sum({ it -> it['valor_atual'] })
        def nomeArquivo = "situacaoCarteira${dataReferencia}.csv"

        new File('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', nomeArquivo).withWriter('utf-8') { writer ->
            writer.writeLine('ativo;tipo;dataEntrada;dataSituacao;qtde;valorInvestido;valorAtual;rentabilidade(%);alocacaoAtual(%)')
            situacaoCarteira.each { it ->
                def valorInvestido = String.valueOf(it['valor_investido']).replace('.', ',')
                def valorAtual = String.valueOf(it['valor_atual']).replace('.', ',')
                def rentabilidade = String.valueOf((it['valor_atual'] / it['valor_investido'])-1).replace('.', ',')
                def alocacao = String.valueOf(it['valor_atual'] / valorTotalAtual).replace('.', ',')

                writer.writeLine("${it['ticker']};${it['tipo']};${it['data_entrada']};${it['data']};${it['qtde_disponivel']};${valorInvestido};${valorAtual};${rentabilidade};${alocacao}")
            }
        }
    }

}

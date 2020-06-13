package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.SituacaoCarteira
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.exception.QuantidadeTituloException
import br.com.carteira.repository.SituacaoCarteiraRepository
import br.com.carteira.repository.AtivoRepository
import groovy.sql.GroovyRowResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class SituacaoCarteiraService {

    AtivoRepository ativoRepository
    SituacaoCarteiraRepository situacaoCarteiraRepository

    @Autowired
    SituacaoCarteiraService(AtivoRepository ativoRepository,
                            SituacaoCarteiraRepository situacaoCarteiraRepository) {
        this.ativoRepository = ativoRepository
        this.situacaoCarteiraRepository = situacaoCarteiraRepository
    }

    @Transactional
    void importarSituacaoAtivos(String caminhoArquivo, String nomeArquivo, LocalDate dataReferencia) {
        new File(caminhoArquivo, nomeArquivo)
                .collect { it -> it.split('\\t') }
                .findAll { it.length == 6 && it[0] != 'Papel' }
                .each { it ->
                    def ativos = ativoRepository.getAllByIdentificadorTipo(it[5], it[0])
                    ativos.each {ativo ->
                        SituacaoCarteira situacaoCarteira = montaSituacao(it, dataReferencia, ativo)
                        incluir(situacaoCarteira)

                    }
                }
    }

    SituacaoCarteira montaSituacao(String[] linhaArquivo, LocalDate dataReferencia, Ativo ativo) {
        if (ativo == null) {
            throw new QuantidadeTituloException("O ativo ${linhaArquivo[0]} não foi encontrado")
        }
        def quantidadeInformadaEmCarteira = new BigDecimal(linhaArquivo[2].replace(',', '.'))

        if (ativo.valorTotalInvestido < 0) {
            //posicao short
            quantidadeInformadaEmCarteira *= -1
        }
        def tipoEhDebCriTesouroFundo = ativo.tipo in TipoAtivoEnum.getDebCriTesouroFundo()
        if (ativo.qtde != quantidadeInformadaEmCarteira && !tipoEhDebCriTesouroFundo) {
            throw new QuantidadeTituloException("A quantidade informada no arquivo precisa ser igual à quantidade atual disponível para o título. Titulo com falha: $ativo.ticker")
        }
        def tipoEhTesouroFundo = ativo.tipo in TipoAtivoEnum.getTesouroFundo()
        def valorAtual = new BigDecimal(linhaArquivo[4].replace(',', '.'))
        if(tipoEhTesouroFundo) {
            valorAtual = valorAtual / quantidadeInformadaEmCarteira * ativo.qtde
        }

        new SituacaoCarteira(
                data: dataReferencia,
                idAtivo: ativo.id,
                qtdeDisponivel: ativo.qtde,
                valorInvestido: ativo.valorTotalInvestido,
                valorAtual: valorAtual
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
                Object rentabilidade = defineRentabilidade(it)
                def alocacao = String.valueOf(it['valor_atual'] / valorTotalAtual).replace('.', ',')

                writer.writeLine("${it['ticker']};${it['tipo']};${it['data_entrada']};${it['data']};${it['qtde_disponivel']};${valorInvestido};${valorAtual};${rentabilidade};${alocacao}")
            }
        }
    }

    private String defineRentabilidade(GroovyRowResult situacaoRowResult) {
        def rentabilidade = String.valueOf((situacaoRowResult['valor_atual'] / situacaoRowResult['valor_investido']) - 1).replace('.', ',')

        rentabilidade
    }

}

package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.ExportacaoSituacaoDTO
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
    void importarSituacaoAtivos(String caminhoArquivo, String nomeArquivo, LocalDate dataReferencia, BigDecimal valorDolarReferencia) {
        new File(caminhoArquivo, nomeArquivo)
                .collect { it -> it.split('\\t') }
                .findAll { it.length == 6 && it[0] != 'Papel' }
                .each { it ->
                    def ativos = ativoRepository.getAllByIdentificadorTipoComSaldo(it[5], it[0])
                    ativos.each {ativo ->
                        SituacaoCarteira situacaoCarteira = montaSituacao(it, dataReferencia, ativo, valorDolarReferencia)
                        incluir(situacaoCarteira)

                    }
                }
    }

    SituacaoCarteira montaSituacao(String[] linhaArquivo, LocalDate dataReferencia, Ativo ativo, BigDecimal valorDolarReferencia) {
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
        def valorAtualDolares = null
        if(ativo.tipo == TipoAtivoEnum.aus) {
            valorAtualDolares = valorAtual
            valorAtual = valorAtual * valorDolarReferencia
        }

        new SituacaoCarteira(
                data: dataReferencia,
                idAtivo: ativo.id,
                qtdeDisponivel: ativo.qtde,
                valorInvestido: ativo.valorTotalInvestido,
                valorInvestidoDolares: ativo.valorInvestidoDolares,
                valorAtual: valorAtual,
                valorAtualDolares: valorAtualDolares
        )
    }

    Long incluir(SituacaoCarteira situacaoCarteira) {
        situacaoCarteiraRepository.incluir(situacaoCarteira)
    }

    void geraSituacaoCarteira(String caminho, LocalDate dataReferencia) {
        def situacaoCarteira = situacaoCarteiraRepository.listaTodosPorDataReferencia(dataReferencia)
        def valorTotalAtual = situacaoCarteira.sum({ it -> it['valor_atual'] })
        def nomeArquivo = "situacaoCarteira${dataReferencia}.csv"

        situacaoCarteira = situacaoCarteira.collect {it ->
            TipoAtivoEnum tipoAtivoEnum = it['tipo']
            def identificador = tipoAtivoEnum.getIdEmSituacaoCarteira()
            def valorInvestido = it['valor_investido']
            def valorAtual = it['valor_atual']
            def alocacao = it['valor_atual'] / valorTotalAtual
            new ExportacaoSituacaoDTO(
                    ativo: it[identificador],
                    tipo: tipoAtivoEnum,
                    dataEntrada: it['data_entrada'].toLocalDate(),
                    dataSituacao: it['data'].toLocalDate(),
                    qtde: it['qtde_disponivel'],
                    valorInvestido: valorInvestido,
                    valorAtual: valorAtual,
                    alocacaoAtual: alocacao
            )}.groupBy {it['ativo']}
                .collectEntries {[(it.key): ['tipo': it.value['tipo'][0],
                                             'dataEntrada': it.value.min {it['dataEntrada']}['dataEntrada'],
                                             'dataSituacao': it.value['dataSituacao'][0],
                                             'qtde': it.value.sum {it['qtde']},
                                             'valorInvestido': (it.value.sum {it['valorInvestido']} as String).replace('.', ','),
                                             'valorAtual': (it.value.sum {it['valorAtual']} as String).replace('.', ','),
                                             'alocacaoAtual': (it.value.sum {it['valorAtual']} / valorTotalAtual as String).replace('.', ',')
                ]]}

        new File('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', nomeArquivo).withWriter('utf-8') { writer ->
            writer.writeLine('ativo;tipo;dataEntrada;dataSituacao;qtde;valorInvestido;valorAtual;alocacaoAtual(%)')
            situacaoCarteira.each { it ->
                writer.writeLine("${it.key};${it.value.tipo};${it.value.dataEntrada};${it.value.dataSituacao};${it.value.qtde};${it.value.valorInvestido};${it.value.valorAtual};${it.value.alocacaoAtual}")
            }
        }
    }

    private String defineRentabilidade(GroovyRowResult situacaoRowResult) {
        def rentabilidade = String.valueOf((situacaoRowResult['valor_atual'] / situacaoRowResult['valor_investido']) - 1).replace('.', ',')

        rentabilidade
    }

}

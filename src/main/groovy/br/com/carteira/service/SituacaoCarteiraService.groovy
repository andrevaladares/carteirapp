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
import java.util.Map.Entry

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
        def contadorAtivosProcessados = 0
        new File(caminhoArquivo, nomeArquivo)
                .collect { it -> it.split('\\t') }
                .findAll { it.length == 6 && it[0] != 'Papel' }
                .each { it ->
                    def ativos = ativoRepository.getAllByIdentificadorTipoComSaldo(it[5], it[0])
                    ativos.each {ativo ->
                        SituacaoCarteira situacaoCarteira = montaSituacao(it, dataReferencia, ativo, valorDolarReferencia)
                        incluir(situacaoCarteira)
                        contadorAtivosProcessados++
                    }
                }
        def ativosNaBaseComSaldo = ativoRepository.listAllComSaldo()
        if (ativosNaBaseComSaldo.size()!=contadorAtivosProcessados) {
            println("ATENÇÃO: A QTDE DE ATIVOS COM SALDO DIFERE DA QTDE ATIVOS PROCESSADOS AGORA")
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
            throw new QuantidadeTituloException("""A quantidade informada no arquivo precisa ser igual à 
                                                    quantidade atual disponível para o título. 
                                                    Titulo com falha: $ativo.ticker / $ativo.cnpjFundo / $ativo.nome""")
        }
        def tipoEhTesouroFundo = ativo.tipo in TipoAtivoEnum.getTesouroFundo()
        def valorAtual = new BigDecimal(linhaArquivo[4].replace(',', '.'))
        if(tipoEhTesouroFundo) {
            valorAtual = valorAtual / quantidadeInformadaEmCarteira * ativo.qtde
        }
        def valorAtualDolares = 0
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
        //trata books padrão
        String[] booksAExcluir = ["extra", "emergência"]
        def situacaoCarteira = situacaoCarteiraRepository.listaTodosPorDataReferenciaComExcecaoDe(dataReferencia, booksAExcluir)
        def valorTotalAtual = situacaoCarteira.sum({ it -> it['valor_atual'] }) as BigDecimal
        def nomeArquivo = "situacaoCarteira${dataReferencia}.csv"

        situacaoCarteira = situacaoCarteira.collect {geraExportacaoDTO(it, valorTotalAtual)}
                .groupBy {it['ativo']}
                .collectEntries {geraMapaValores(it, valorTotalAtual)}

        //trata books extras
        booksAExcluir = ['fii', 'ações', 'metais', 'tático', 'renda fixa', 'moedas', 'internacional']
        def situacaoExtras = situacaoCarteiraRepository.listaTodosPorDataReferenciaComExcecaoDe(dataReferencia, booksAExcluir)
        valorTotalAtual = situacaoExtras.sum({ it -> it['valor_atual'] }) as BigDecimal
        situacaoExtras = situacaoExtras.collect {  geraExportacaoDTO(it, valorTotalAtual)}
                .groupBy {it['ativo']}
                .collectEntries {geraMapaValores(it, valorTotalAtual)}

        new File(caminho, nomeArquivo).withWriter() { writer ->
            writer.writeLine('ativo;tipo;book;dataEntrada;dataSituacao;qtde;valorInvestido;valorInvestidoDolares;valorAtual;valorAtualDolares;rentabilidade(%);alocacaoAtual(%)')
            situacaoCarteira.each { it ->
                writer.writeLine("${it.key};${it.value.tipo};${it.value.book};${it.value.dataEntrada};${it.value.dataSituacao};${it.value.qtde};${it.value.valorInvestido};${it.value.valorInvestidoDolares};${it.value.valorAtual};${it.value.valorAtualDolares};${it.value.rentabilidade};${it.value.alocacaoAtual}")
            }
            writer.writeLine('')
            writer.writeLine('')
            situacaoExtras.each { it ->
                writer.writeLine("${it.key};${it.value.tipo};${it.value.book};${it.value.dataEntrada};${it.value.dataSituacao};${it.value.qtde};${it.value.valorInvestido};${it.value.valorInvestidoDolares};${it.value.valorAtual};${it.value.valorAtualDolares};${it.value.rentabilidade};${it.value.alocacaoAtual}")
            }
        }
    }

    Map geraMapaValores(Entry<Object, List<ExportacaoSituacaoDTO>> valoresAtivo, BigDecimal valorTotalAtual) {
        def rentabilidade = valoresAtivo.value.sum {it['valorAtual']} / valoresAtivo.value.sum {it['valorInvestido']} - 1
        [(valoresAtivo.key): ['tipo'                 : valoresAtivo.value['tipo'][0],
                              'book'                 : valoresAtivo.value['bookAtivo'][0],
                              'dataEntrada'          : valoresAtivo.value.min {it['dataEntrada']}['dataEntrada'],
                              'dataSituacao'         : valoresAtivo.value['dataSituacao'][0],
                              'qtde'                 : (valoresAtivo.value.sum {it['qtde']} as String).replace('.', ','),
                              'valorInvestido'       : (valoresAtivo.value.sum {it['valorInvestido']} as String).replace('.', ','),
                              'valorInvestidoDolares': (valoresAtivo.value.sum {it['valorInvestidoDolares']} as String).replace('.', ','),
                              'valorAtual'           : (valoresAtivo.value.sum {it['valorAtual']} as String).replace('.', ','),
                              'valorAtualDolares'    : (valoresAtivo.value.sum {it['valorAtualDolares']} as String)?.replace('.', ','),
                              'rentabilidade'        : (rentabilidade as String)?.replace('.', ','),
                              'alocacaoAtual'        : (valoresAtivo.value.sum {it['valorAtual']} / valorTotalAtual as String).replace('.', ',')
        ]]
    }

    ExportacaoSituacaoDTO geraExportacaoDTO(GroovyRowResult groovyRowResult, BigDecimal valorTotalAtual) {
        TipoAtivoEnum tipoAtivoEnum = groovyRowResult['tipo'] as TipoAtivoEnum
        def identificador = tipoAtivoEnum.getIdEmSituacaoCarteira()
        def valorInvestido = groovyRowResult['valor_investido'] as BigDecimal
        def valorAtual = groovyRowResult['valor_atual'] as BigDecimal
        def alocacao = groovyRowResult['valor_atual'] / valorTotalAtual
        new ExportacaoSituacaoDTO(
                ativo: groovyRowResult[identificador],
                tipo: tipoAtivoEnum,
                bookAtivo: groovyRowResult['book'],
                dataEntrada: groovyRowResult['data_entrada'].toLocalDate(),
                dataSituacao: groovyRowResult['data'].toLocalDate(),
                qtde: groovyRowResult['qtde_disponivel'],
                valorInvestido: valorInvestido,
                valorInvestidoDolares: groovyRowResult['valor_investido_dolares'],
                valorAtual: valorAtual,
                valorAtualDolares: groovyRowResult['valor_atual_dolares'],
                alocacaoAtual: alocacao
        )
    }
}

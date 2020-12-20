package br.com.carteira.scripts

import br.com.carteira.entity.OperacaoComeCotasDTO
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.impostos.ConsolidacaoImpostos
import br.com.carteira.service.AtivoService
import br.com.carteira.service.OperacaoService
import br.com.carteira.service.SituacaoCarteiraService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.YearMonth

@Component
class ImportarDados {

    @Autowired
    OperacaoService operacaoService
    @Autowired
    SituacaoCarteiraService situacaoCarteiraService
    @Autowired
    AtivoService ativoService

    void importarOperacoes(String caminho, String arquivo) {
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarArquivoOperacao(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaNegociacao(String caminho, String arquivo) {
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarOperacoesNotaNegociacao(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaNegociacaoAcoesInternacionais(String caminho, String arquivo) {
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarOperacoesNotaNegociacaoUs(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaInvestimento(String caminho, String arquivo) {
        println 'Iniciando importação de Nota de Investimento'
        println '==============='

        operacaoService.importarOperacoesNotaInvestimento(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de Nota de Investimento'

    }

    void importarOperacoesComeCotas(String cnpj, LocalDate dataOperacao, List<OperacaoComeCotasDTO> operacaoComeCotasDTOList) {
        println 'Iniciando importação de Nota de Investimento'
        println '==============='

        operacaoService.incluiOperacoesComeCotas(cnpj, dataOperacao, operacaoComeCotasDTOList)

        println '==============='
        println 'Encerrada importação de Nota de Investimento'

    }

    void importarSituacaoCarteira(String caminho, String arquivo, LocalDate dataReferencia, BigDecimal valorDolarReferencia) {
        println 'Iniciando importação de situacao da carteira'
        println '==============='

        situacaoCarteiraService.importarSituacaoAtivos(caminho, arquivo, dataReferencia, valorDolarReferencia)

        println '==============='
        println 'Encerrada importação de situação da carteira'

    }

    void atribuirBook(String nomeBook, TipoAtivoEnum tipoDoAtivo, String identificadorAtivo) {
        ativoService.atribuirBook(nomeBook, tipoDoAtivo, identificadorAtivo)
    }
}

@Component
class ExportarDados {
    @Autowired
    SituacaoCarteiraService situacaoCarteiraService
    @Autowired
    ConsolidacaoImpostos geradorImpostos

    void exportarSituacaoCarteira(String caminho, LocalDate dataReferencia) {
        println 'Iniciando exportação de situacao da carteira'
        println '==============='

        situacaoCarteiraService.geraSituacaoCarteira(caminho, dataReferencia)

        println '==============='
        println 'Encerrada exportação de situação da carteira'

    }

    void calcularImpostoAPagar(YearMonth mesAno, List<TipoAtivoEnum> tiposDeAtivo) {
        tiposDeAtivo.each {
            println "Calculando imposto a pagar de ${it} para ${mesAno}"
            geradorImpostos.consolidarImpostos(mesAno, it)
        }
    }

    void calcularImpostoAPagar(YearMonth mesAno) {
        println "Calculando impostos a pagar para ${mesAno}"
        geradorImpostos.consolidarImpostos(mesAno)
    }
}

@Component
class AtuarSobreOsDados {
    @Autowired
    OperacaoService operacaoService

    void desdobrarAcao(LocalDate data, String ticker, Integer multiplicador) {
        operacaoService.desdobrarAcao(data, ticker, multiplicador)
    }

    void agruparAcao(LocalDate data, String ticker, Integer divisor) {
        operacaoService.agruparAcao(data, ticker, divisor)
    }

    void lancarDividendo(String tickerMoedaFoco, LocalDate dataDividendo, String identificadorAtivoGerador, BigDecimal valorDividendo) {
        operacaoService.lancarDividendo(tickerMoedaFoco, dataDividendo, identificadorAtivoGerador, valorDividendo)
    }

    void lancarTaxas(String tickerMoedaFoco, LocalDate data, String identificadorAtivoGerador, BigDecimal valorTaxa) {
        operacaoService.lancarTaxas(tickerMoedaFoco, data, identificadorAtivoGerador, valorTaxa)
    }

    void lancarJurosTesouroDireto(LocalDate data, String nomeTituloTesouro, BigDecimal valor) {
        operacaoService.lancarJuroTesouro(data, nomeTituloTesouro, valor)
    }
}


ApplicationContext context =
        new ClassPathXmlApplicationContext('applicationContext.xml')

ImportarDados importarDados = context.getBean(ImportarDados.class)
ExportarDados exportarDados = context.getBean(ExportarDados.class)
AtuarSobreOsDados atuarSobreOsDados = context.getBean(AtuarSobreOsDados.class)

//========= Execute aqui o metodo que deseja

//importarDados.importarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras\\202009', 'situacaoCarteiraCompleta.txt', LocalDate.of(2020, 9, 30), 5.6401)
//exportarDados.exportarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras\\202009', LocalDate.of(2020,9,30))
//importarDados.importarOperacoes('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'operacoesAcoesFIIs_ate_012020_2.txt')
//importarDados.importarNotaNegociacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras\\202009', '20200914_NotaCorretagem_XP_ipo_petz.txt')
//importarDados.importarNotaNegociacaoAcoesInternacionais('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras\\202009', '20200917_notaAvenue_meli.txt')
//importarDados.importarNotaInvestimento('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras\\202009', '20200918_NotaInvestimento_TesouroSelicXP.txt')
//O valor tem que ser líquido de impostos, o  for o caso (quando aparecer o imposto discriminado)
atuarSobreOsDados.lancarDividendo('brl', LocalDate.of(2020, 10, 1), 'ITUB4', 3.9)
//atuarSobreOsDados.lancarTaxas('brl', LocalDate.of(2020, 9, 21), 'oz2', 68.45)
//Lançamento líquido já de impostos e taxas
//atuarSobreOsDados.lancarJurosTesouroDireto(LocalDate.of(2020, 8, 17), 'Tesouro IPCA+ com Juros Semestrais 2050', 514.62)
//importarDados.atribuirBook('renda fixa', TipoAtivoEnum.tse, 'Tesouro Selic 2025')
/*
importarDados.importarOperacoesComeCotas('29562673000117', LocalDate.of(2019, 05, 29), [
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2019, 4,3), qtdeComeCotas: new BigDecimal(15.38454166)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2020, 1,13), qtdeComeCotas: new BigDecimal(1.37666566)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2020, 2,3), qtdeComeCotas: new BigDecimal(8.91249266)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2020, 4,17), qtdeComeCotas: new BigDecimal(1.42136266)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2020, 4,30), qtdeComeCotas: new BigDecimal(1.49287666))
])
*/
//exportarDados.calcularImpostoAPagar(YearMonth.of(2020, 7), [TipoAtivoEnum.a])
//exportarDados.calcularImpostoAPagar(YearMonth.of(2020, 9))
//atuarSobreOsDados.desdobrarAcao(LocalDate.of(2020,8,31),'aapl', 4)
//atuarSobreOsDados.agruparAcao(LocalDate.of(2020,8,13), 'hbor3', 5)
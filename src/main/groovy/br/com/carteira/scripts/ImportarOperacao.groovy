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

    void importarDividendoAtivoUs(LocalDate dataDividendo, String identificadorAtivoGerador, BigDecimal valorDividendo) {
        operacaoService.importarDividendoAtivoUs(dataDividendo, identificadorAtivoGerador, valorDividendo)
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

ApplicationContext context =
        new ClassPathXmlApplicationContext('applicationContext.xml')

ImportarDados importarDados = context.getBean(ImportarDados.class)
ExportarDados exportarDados = context.getBean(ExportarDados.class)

//========= Execute aqui o metodo que deseja

//importarDados.importarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'situacaoCarteiraCompleta20200731.txt', LocalDate.of(2020, 7, 31), 5.2027)
exportarDados.exportarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', LocalDate.of(2020,7,31))
//importarDados.importarOperacoes('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'operacoesAcoesFIIs_ate_012020_2.txt')
//importarDados.importarNotaNegociacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', '20200525_NotaCorretagem_xp.txt')
//importarDados.importarNotaNegociacaoAcoesInternacionais('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', '20200714_NotaCorretagem_avenue_Square.txt')
//importarDados.importarNotaInvestimento('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', '20200728_NotaInvestimento_btg.txt')
//importarDados.importarDividendoAtivoUs(LocalDate.of(2020, 7, 16), 'ess', 7.88)
//importarDados.atribuirBook('fii', TipoAtivoEnum.fii, 'brcr11')
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
//exportarDados.calcularImpostoAPagar(YearMonth.of(2020, 7))
//O valor tem que ser líquido de impostos, quando for o caso

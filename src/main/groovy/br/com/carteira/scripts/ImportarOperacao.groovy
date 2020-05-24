package br.com.carteira.scripts

import br.com.carteira.entity.OperacaoComeCotasDTO
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.service.OperacaoService
import br.com.carteira.service.SituacaoCarteiraService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component

import java.time.LocalDate

@Component
class ImportarDados {

    @Autowired
    OperacaoService operacaoService
    @Autowired
    SituacaoCarteiraService situacaoCarteiraService

    void importarOperacoes(String caminho, String arquivo){
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarArquivoOperacao(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaNegociacao(String caminho, String arquivo){
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarOperacoesNotaNegociacao(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaInvestimento(String caminho, String arquivo){
        println 'Iniciando importação de Nota de Investimento'
        println '==============='

        operacaoService.importarOperacoesNotaInvestimento(caminho, arquivo)

        println '==============='
        println 'Encerrada importação de Nota de Investimento'

    }

    void importarOperacoesComeCotas(String cnpj, LocalDate dataOperacao, List<OperacaoComeCotasDTO> operacaoComeCotasDTOList){
        println 'Iniciando importação de Nota de Investimento'
        println '==============='

        operacaoService.incluiOperacoesComeCotas(cnpj, dataOperacao, operacaoComeCotasDTOList)

        println '==============='
        println 'Encerrada importação de Nota de Investimento'

    }

    void importarSituacaoCarteira(String caminho, String arquivo, LocalDate dataReferencia){
        println 'Iniciando importação de situacao da carteira'
        println '==============='

        situacaoCarteiraService.importarSituacaoTitulos(caminho, arquivo, dataReferencia)

        println '==============='
        println 'Encerrada importação de situação da carteira'

    }
}

@Component
class ExportarDados {
    @Autowired
    SituacaoCarteiraService situacaoCarteiraService

    void exportarSituacaoCarteira(String caminho, LocalDate dataReferencia) {
        println 'Iniciando exportação de situacao da carteira'
        println '==============='

        situacaoCarteiraService.geraSituacaoCarteira(caminho, dataReferencia)

        println '==============='
        println 'Encerrada exportação de situação da carteira'

    }
}

ApplicationContext context =
        new ClassPathXmlApplicationContext('applicationContext.xml')

ImportarDados importarDados = context.getBean(ImportarDados.class)
ExportarDados exportarDados = context.getBean(ExportarDados.class)

//========= Execute aqui o metodo que deseja

//importarDados.importarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'situacaoCarteiraShort31032020.txt', LocalDate.of(2020, 3, 31))
//exportarDados.exportarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', LocalDate.of(2020,3,31))
//importarDados.importarOperacoes('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'operacoesAcoesFIIs_ate_012020_2.txt')
importarDados.importarNotaNegociacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'notaNegociacaoXp_20200406.2.txt')
//importarDados.importarNotaInvestimento('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'notaInvestimentoXPTesouro_20200319.2.txt')
/*
importarDados.importarOperacoesComeCotas('29562673000117', LocalDate.of(2019, 11, 29), [
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2019, 4,3), qtdeComeCotas: new BigDecimal(47.979523)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2019, 5,6), qtdeComeCotas: new BigDecimal(16.33016)),
        new OperacaoComeCotasDTO(dataAplicacao: LocalDate.of(2019, 5,13), qtdeComeCotas: new BigDecimal(106.21435))
])
*/

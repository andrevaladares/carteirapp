package br.com.carteira.scripts

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
importarDados.importarNotaNegociacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'notaNegociacaoXp_Oz2_17042019.txt')
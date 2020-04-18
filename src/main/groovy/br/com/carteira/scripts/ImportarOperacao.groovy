package br.com.carteira.scripts

import br.com.carteira.service.OperacaoService
import br.com.carteira.service.SituacaoCarteiraService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cglib.core.Local
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

        operacaoService.importarArquivoNotaNegociacao(caminho, arquivo)

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
//importarDados.importarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'situacaoCarteiraFundosImobiliarios28022020.txt', LocalDate.of(2020, 2, 28))
exportarDados.exportarSituacaoCarteira('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', LocalDate.of(2020,2,28))
package br.com.carteira.scripts

import br.com.carteira.service.OperacaoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component

@Component
class ImportarDados {

    @Autowired
    OperacaoService operacaoService

    void importarOperacoes(){
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarArquivoOperacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'operacoesAcoesFIIs_CorrecaoVulc3.txt')

        println '==============='
        println 'Encerrada importação de operações'

    }

    void importarNotaNegociacao(){
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarArquivoNotaNegociacao('C:\\Users\\AndreValadares\\Documents\\OperacoesFinanceiras', 'notaNegociacao27022020.txt')

        println '==============='
        println 'Encerrada importação de operações'

    }
}

ApplicationContext context =
        new ClassPathXmlApplicationContext('applicationContext.xml')

ImportarDados importarDados = context.getBean(ImportarDados.class)

//========= Execute aqui o metodo que deseja
importarDados.importarOperacoes()
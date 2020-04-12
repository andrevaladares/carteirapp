package br.com.carteira.scripts

import br.com.carteira.service.OperacaoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component

@Component
class ImportarOperacoes {

    @Autowired
    OperacaoService operacaoService

    void executar(){
        println 'Iniciando importação de operações'
        println '==============='

        operacaoService.importarArquivoOperacao('C:\\Users\\AndreValadares\\Documents', 'operacoesBolsa2.txt')

        println '==============='
        println 'Encerrada importação de operações'

    }
}

ApplicationContext context =
        new ClassPathXmlApplicationContext('applicationContext.xml')

ImportarOperacoes importarOperaces = context.getBean(ImportarOperacoes.class)
importarOperaces.executar()

package br.com.carteira

import br.com.carteira.service.OperacaoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component

@Component
class Main {

    @Autowired
    OperacaoService operacaoService

    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext('applicationContext.xml');

        Main p = context.getBean(Main.class)
        p.start(args)
    }

    void start(String[] args) {
        if (args.length == 0) {
            naoInformouComandos()
            return
        }
        switch (args[0]) {
            case ComandosEnum.importar_carteira as String:
                operacaoService.teste()
                importarCarteira(args)
                break
            default:
                naoInformouComandos()
                break
        }
    }

    void naoInformouComandos() {
        println('E preciso informar um dos comandos validos: ')
        ComandosEnum.values().each { println(it) }
    }

    void importarCarteira(String[] args) {
        if (args.length != 3) {
            println(''''Argumentos invalidos. A importacao de carteira espera 2 argumentos. 
                    Foram informados ${args.length - 1}''')
            return
        }
        println('Chamado comando: ' + args[0] + ', caminho: ' + args[1] + ', arquivo: ' + args[2])
    }
}
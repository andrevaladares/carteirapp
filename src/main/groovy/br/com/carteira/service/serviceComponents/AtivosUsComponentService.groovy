package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.RegimeResgateEnum
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class AtivosUsComponentService implements ComponentServiceTrait{

    AtivoRepository ativoRepository
    OperacaoRepository operacaoRepository

    @Autowired
    AtivosUsComponentService(AtivoRepository ativoRepository, OperacaoRepository operacaoRepository) {
        this.ativoRepository = ativoRepository
        this.operacaoRepository = operacaoRepository
    }

    void incluiOperacao(String[] linhaAberta, Integer numeroLinha, Long idNotaNegociacao, String dataNegociacao) {
        def operacao
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')
        if (numeroLinha == 0) {
            if (linhaAberta[0] != 'tipo')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            operacao = new Operacao(
                    data: LocalDate.parse(dataNegociacao, dateFormatter),
                    idNotaNegociacao: idNotaNegociacao,
                    tipoOperacao: linhaAberta[0],
                    ativo: Ativo.getInstanceWithAtributeMap(
                            ticker: linhaAberta[1],
                            tipo: linhaAberta[2].toLowerCase() as TipoAtivoEnum
                    ),
                    qtde: new BigDecimal(linhaAberta[4].replace(',', '.')),
                    valorTotalOperacao: new BigDecimal(linhaAberta[5].replace(',', '.'))
            )

            incluir(operacao)
        }

    }

    @Override
    List<Operacao> incluir(Operacao operacaoOriginal) {
        def operacoesGeradas = []
        //Operacao com a acao
        def ativo = ativoRepository.getByTicker(operacaoOriginal.ativo.ticker.toLowerCase())
        Operacao operacaoAcao = operacaoOriginal
        if (ativo != null) {
            operacaoAcao.ativo = ativo
            operacaoAcao = complementarOperacao(operacaoAcao)[0] //Essa linha sempre gerará apenas uma operação
            Ativo ativoParaAtualizacao = operacaoOriginal.ativo.atualizarAtivoAPartirDaOperacao(operacaoAcao)
            ativoRepository.atualizar(ativoParaAtualizacao)
        } else {
            operacaoAcao.ativo = criarAtivoAPartirDaOperacao(operacaoAcao)
            operacaoAcao = complementarOperacao(operacaoAcao)[0] //Essa linha sempre gerará apenas uma operação
            operacaoAcao.ativo.id = ativoRepository.incluir(operacaoAcao.ativo)
        }
        operacaoAcao.id = operacaoRepository.incluir(operacaoAcao)
        operacoesGeradas << operacaoAcao

        //Operacao com os dolares
        Ativo dolar = ativoRepository.getByTicker('us$')
        Operacao operacaoDolar = new Operacao(
                idNotaNegociacao: operacaoOriginal.idNotaNegociacao,
                data: operacaoOriginal.data,
                ativo: dolar,
                qtde: operacaoOriginal.valorTotalOperacao, //valores de operações em dolar alterando o saldo de dolares
        )
        if(operacaoOriginal.tipoOperacao == TipoOperacaoEnum.c) {
            //Em caso de compra da ação US, é necessária uma transferência de saída de dolares
            if (operacaoDolar.ativo == null || operacaoDolar.ativo.qtde == 0){
                throw new OperacaoInvalidaException('Não é permitido comprar ações US sem dolares disponíveis em carteira')
            }
            operacaoDolar.tipoOperacao = TipoOperacaoEnum.ts
            def custoMedioDolarAtual = dolar.valorTotalInvestido / dolar.qtde
            operacaoDolar.valorTotalOperacao = operacaoDolar.qtde * custoMedioDolarAtual
            operacaoDolar.ativo.atualizarAtivoAPartirDaOperacao(operacaoDolar)
            operacaoDolar.id = operacaoRepository.incluir(operacaoDolar)
            ativoRepository.atualizar(operacaoDolar.ativo)
        }
        else {
            //Em caso de venda de ação US, devo gerar um crédito de dolares equivalente ao valor total recebido pela venda
            if(operacaoDolar.ativo == null) {
                operacaoDolar.ativo = Ativo.getInstanceWithAtributeMap(
                        ticker: 'us$',
                        tipo: TipoAtivoEnum.aus,
                        nome: 'dolar',
                        qtde: 0,
                        valorTotalInvestido: 0,
                        dataEntrada: LocalDate.now()
                )
            }
            operacaoDolar.tipoOperacao = TipoOperacaoEnum.te
            def custoMedioDolarAtual =  operacaoDolar.ativo.qtde != 0 ? operacaoDolar.ativo.valorTotalInvestido / operacaoDolar.ativo.qtde : 0
            operacaoDolar.valorTotalOperacao = operacaoDolar.qtde * custoMedioDolarAtual
            operacaoDolar.ativo.atualizarAtivoAPartirDaOperacao(operacaoDolar)
            operacaoDolar.id = operacaoRepository.incluir(operacaoDolar)
            ativoRepository.atualizar(operacaoDolar.ativo)
        }
        operacoesGeradas << operacaoDolar
    }
}

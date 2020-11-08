package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
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
class AtivosUsComponentService implements ComponentServiceTrait {

    AtivoRepository ativoRepository
    OperacaoRepository operacaoRepository

    @Autowired
    AtivosUsComponentService(AtivoRepository ativoRepository, OperacaoRepository operacaoRepository) {
        this.ativoRepository = ativoRepository
        this.operacaoRepository = operacaoRepository
    }

    void incluiOperacao(String[] linhaAberta, Integer numeroLinha, Long idNotaNegociacao, String dataNegociacao, BigDecimal valorDolarNaData) {
        def operacao
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')
        if (numeroLinha == 0) {
            if (linhaAberta[0] != 'tipo')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            def valorOperacaoDolares = new BigDecimal(linhaAberta[5].replace(',', '.'))
            operacao = new Operacao(
                    data: LocalDate.parse(dataNegociacao, dateFormatter),
                    idNotaNegociacao: idNotaNegociacao,
                    tipoOperacao: linhaAberta[0],
                    ativo: Ativo.getInstanceWithAtributeMap(
                            ticker: linhaAberta[1],
                            tipo: linhaAberta[2].toLowerCase() as TipoAtivoEnum
                    ),
                    qtde: new BigDecimal(linhaAberta[4].replace(',', '.')),
                    valorOperacaoDolares: valorOperacaoDolares,
                    valorTotalOperacao: valorOperacaoDolares * valorDolarNaData
            )

            incluir(operacao)
        }

    }

    @Override
    List<Operacao> incluir(Operacao operacaoOriginal) {
        def operacoesGeradas = []

        List<Operacao> operacaoDolarList = gerarTransferenciasDeDolares(operacaoOriginal)

        //Operacao com a acao
        def ativo = ativoRepository.getByTicker(operacaoOriginal.ativo.ticker.toLowerCase())
        Operacao operacaoAcao = operacaoOriginal
        if (ativo != null) {
            operacaoAcao.ativo = ativo
            operacaoAcao = complementarOperacao(operacaoAcao)[0] //Essa linha sempre gerará apenas uma operação
            Ativo ativoParaAtualizacao = operacaoAcao.ativo.atualizarAtivoAPartirDaOperacao(operacaoAcao)
            ativoRepository.atualizar(ativoParaAtualizacao)
        } else {
            operacaoAcao.ativo = criarAtivoAPartirDaOperacao(operacaoAcao)
            operacaoAcao = complementarOperacao(operacaoAcao)[0] //Essa linha sempre gerará apenas uma operação
            operacaoAcao.ativo.id = ativoRepository.incluir(operacaoAcao.ativo)
        }

        operacaoAcao.id = operacaoRepository.incluir(operacaoAcao)
        operacoesGeradas << operacaoAcao
        operacaoDolarList.each {
            operacaoRepository.incluir(it)
            ativoRepository.atualizar(it.ativo)
        }
        operacoesGeradas.addAll(operacaoDolarList)

        return operacoesGeradas
    }

    private List<Operacao> gerarTransferenciasDeDolares(Operacao operacaoOriginal) {
        def operacaoList = []
        Ativo dolarDoExterior = ativoRepository.getByTicker('us$')
        Ativo dolarDoBrasil = ativoRepository.getByTicker('br$')
        Operacao operacaoDolarExterior
        Operacao operacaoDolarBrasil
        if (operacaoOriginal.tipoOperacao == TipoOperacaoEnum.c) {
            //Em caso de compra da ação US, é necessária uma transferência de saída de dolares. Tenta primeiro usar
            // dolares orinados do exterior (melhor para impostos) e usa dolares transferidos do Brasil quando
            // necessário

            validarQtdesDolaresVsAcaoComprada(dolarDoExterior, dolarDoBrasil, operacaoOriginal)
            def sobraOperacaoDolar = 0.0
            def qtdeDolaresExteriorOperacao
            if (operacaoOriginal.valorOperacaoDolares <= dolarDoExterior.qtde) {
                qtdeDolaresExteriorOperacao = operacaoOriginal.valorOperacaoDolares
                //valores de operações em dolar alterando o saldo de dolares
            } else {
                qtdeDolaresExteriorOperacao = dolarDoExterior.qtde
                sobraOperacaoDolar = operacaoOriginal.valorOperacaoDolares - dolarDoExterior.qtde

            }
            if (qtdeDolaresExteriorOperacao > 0) {
                operacaoDolarExterior = new Operacao(
                        idNotaNegociacao: operacaoOriginal.idNotaNegociacao,
                        data: operacaoOriginal.data,
                        ativo: dolarDoExterior,
                        qtde: qtdeDolaresExteriorOperacao,
                        tipoOperacao: TipoOperacaoEnum.ts
                )
                def custoMedioDolarExteriorAtual = dolarDoExterior.valorTotalInvestido / dolarDoExterior.qtde
                operacaoDolarExterior.valorTotalOperacao = operacaoDolarExterior.qtde * custoMedioDolarExteriorAtual
                operacaoDolarExterior.ativo.atualizarAtivoAPartirDaOperacao(operacaoDolarExterior)

                operacaoList << operacaoDolarExterior

            }

            if (sobraOperacaoDolar > 0) {
                operacaoDolarBrasil = new Operacao(
                        idNotaNegociacao: operacaoOriginal.idNotaNegociacao,
                        data: operacaoOriginal.data,
                        ativo: dolarDoBrasil,
                        qtde: sobraOperacaoDolar,
                        tipoOperacao: TipoOperacaoEnum.ts
                )
                def custoMedioDolarBrasilAtual = dolarDoBrasil.valorTotalInvestido / dolarDoBrasil.qtde
                operacaoDolarBrasil.valorTotalOperacao = operacaoDolarBrasil.qtde * custoMedioDolarBrasilAtual
                operacaoDolarBrasil.ativo.atualizarAtivoAPartirDaOperacao(operacaoDolarBrasil)

                operacaoList << operacaoDolarBrasil
            }

        } else {
            //Em caso de venda de ação US, devo gerar um crédito de dolares do tipo originário do exterior
            // equivalente ao valor total recebido pela venda
            operacaoDolarExterior = new Operacao(
                    idNotaNegociacao: operacaoOriginal.idNotaNegociacao,
                    data: operacaoOriginal.data,
                    ativo: dolarDoExterior,
                    qtde: operacaoOriginal.valorOperacaoDolares, //valores de operações em dolar alterando o saldo de dolares
                    tipoOperacao: TipoOperacaoEnum.te
            )
            if (operacaoDolarExterior.ativo == null) {
                operacaoDolarExterior.ativo = Ativo.getInstanceWithAtributeMap(
                        ticker: 'us$',
                        tipo: TipoAtivoEnum.aus,
                        nome: 'dolar',
                        qtde: 0,
                        valorTotalInvestido: 0,
                        dataEntrada: LocalDate.now(),
                )
            }
            def custoMedioDolarAtual = operacaoDolarExterior.ativo.qtde != 0 ? operacaoDolarExterior.ativo.valorTotalInvestido / operacaoDolarExterior.ativo.qtde : 0
            operacaoDolarExterior.valorTotalOperacao = operacaoDolarExterior.qtde * custoMedioDolarAtual
            operacaoDolarExterior.ativo.atualizarAtivoAPartirDaOperacao(operacaoDolarExterior)

            operacaoList << operacaoDolarExterior
        }
        operacaoList
    }

    /**
     * Atualiza na operação o custo médio e o resultado da venda
     *
     * @param operacao a operação de referência
     * @return a operação atualizada
     */
    @Override
    List<Operacao> complementarOperacao(Operacao operacao) {
        if (operacao.tipoOperacao == TipoOperacaoEnum.v && operacao.ativo.qtde > 0) {
            //operacao de venda comum (redução de posição comprada)
            operacao.custoMedioOperacao = operacao.ativo.obterCustoMedioUnitario()
            operacao.custoMedioDolares = operacao.ativo.obterCustoMedioUnitarioDolares()
            operacao.resultadoVenda = operacao.ativo.obterResultadoVenda(operacao.custoMedioOperacao, operacao.valorTotalOperacao, operacao.qtde)
            operacao.resultadoVendaDolares = operacao.valorOperacaoDolares - (operacao.custoMedioDolares * operacao.qtde)
        } else if (operacao.tipoOperacao == TipoOperacaoEnum.c && operacao.ativo.qtde < 0) {
            //Reducao de um short
            operacao.custoMedioOperacao = operacao.ativo.obterCustoMedioUnitario()
            operacao.custoMedioOperacao = operacao.ativo.obterCustoMedioUnitarioDolares()
            operacao.resultadoVenda = (operacao.valorTotalOperacao - BigDecimal.valueOf(operacao.custoMedioOperacao * operacao.qtde)) * -1
            operacao.resultadoVendaDolares = (operacao.valorOperacaoDolares - (operacao.custoMedioDolares * operacao.qtde)) * -1
        }

        [operacao]
    }

    void validarQtdesDolaresVsAcaoComprada(Ativo dolarExterior, Ativo dolarBrasil, Operacao operacaoOriginal) {
        if ((dolarExterior == null || dolarExterior.qtde == 0) && (dolarBrasil == null || dolarBrasil.qtde == 0)) {
            throw new OperacaoInvalidaException('Não é permitido comprar ações US sem dolares disponíveis em carteira')
        }
        def qtdeDolarExterior = dolarExterior != null ? dolarExterior.qtde : 0.0
        def qtdeDolarBrasil = dolarBrasil != null ? dolarBrasil.qtde : 0.0

        if ((qtdeDolarExterior + qtdeDolarBrasil) < operacaoOriginal.valorOperacaoDolares) {
            throw new OperacaoInvalidaException('Não é permitido comprar ações US sem dolares disponíveis em carteira')
        }
    }

}

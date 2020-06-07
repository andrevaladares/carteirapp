package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.NotaInvestimento
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.OperacaoComeCotasDTO
import br.com.carteira.entity.RegimeResgateEnum
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.TipoOperacaoEnum
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class FundosInvestimentosServiceComponent implements ComponentServiceTrait{
    AtivoRepository ativoRepository
    OperacaoRepository operacaoRepository

    @Autowired
    FundosInvestimentosServiceComponent(AtivoRepository ativoRepository, OperacaoRepository operacaoRepository) {
        this.ativoRepository = ativoRepository
        this.operacaoRepository = operacaoRepository
    }

    void incluiOperacao(String[] linhaArquivo, int numeroLinha, NotaInvestimento notaInvestimento, LocalDate dataOperacao) {
        def operacao

        if (numeroLinha == 0) {
            if (linhaArquivo[0] != 'tipo')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            def qtde = new BigDecimal(linhaArquivo[5].replace(",", "."))
            def valorTotalOperacao = new BigDecimal(linhaArquivo[4].replace(",", "."))
            def custoMedioOperacao = valorTotalOperacao.divide(qtde, 8, RoundingMode.HALF_UP)
            def cnpjFundo = linhaArquivo[1]
            def tipoAtivo = linhaArquivo[3]
            operacao = new Operacao(
                    data: dataOperacao,
                    notaInvestimento: notaInvestimento,
                    tipoOperacao: linhaArquivo[0],
                    ativo: Ativo.getInstanceWithAtributeMap(
                            nome: linhaArquivo[2],
                            tipo: tipoAtivo,
                            cnpjFundo: cnpjFundo
                    ),
                    qtde: qtde,
                    valorTotalOperacao: valorTotalOperacao,
                    custoMedioOperacao: custoMedioOperacao
            )

            incluir(operacao)
        }
    }

    /**
     * Inclui uma operação para um fundo de investimento
     * Junto com a nova operação, atualiza os valores de estoque do título (quantidade e valor total investido até então)
     *
     * @param operacao a operação a ser inserida
     * @return a operacao gravada
     */
    List<Operacao> incluir(Operacao operacao) {
        def operacoesGeradas = []
        if(operacao.tipoOperacao == TipoOperacaoEnum.c) {
            //Em caso de compra (aplicação) é sempre criada uma única operação
            operacao.ativo = criarAtivoAPartirDaOperacao(operacao)
            operacao.ativo.id = ativoRepository.incluir(operacao.ativo)
            operacoesGeradas << operacaoRepository.incluir(operacao)
        }
        else {
            def ordenacao = operacao.notaInvestimento.regimeResgate == RegimeResgateEnum.fifo ? 'asc' : 'desc'
            def ativos = ativoRepository.getAllByAtivoExample(operacao.ativo, ordenacao)
            if (ativos.isEmpty()) {
                //Tentando vender sem estoque. Não pode haver short
                throw new OperacaoInvalidaException("operação inválida. Não pode haver um short de fundo de investimento. CNPJ do fundo: $operacao.ativo.cnpjFundo")
            }
            else {
                //Existe 'estoque' do ativo
                operacoesGeradas = complementarOperacao(operacao, ativos)
                operacoesGeradas.each {operacaoGerada ->
                    Ativo ativoParaAtualizacao = operacaoGerada.ativo
                    ativoParaAtualizacao = ativoParaAtualizacao.atualizarAtivoAPartirDaOperacao(operacaoGerada)
                    operacaoRepository.incluir(operacaoGerada)
                    ativoRepository.atualizar(ativoParaAtualizacao)
                }
            }
        }
        operacoesGeradas
    }

    Ativo criarAtivoAPartirDaOperacao(Operacao operacao) {
        /*
         Fundo de investimento será criado sempre a partir de uma operação de compra
         */
        if (operacao.tipoOperacao == TipoOperacaoEnum.v) {
            throw new OperacaoInvalidaException("operação inválida. Não pode haver um short de fundo de investimento. CNPJ do fundo: $operacao.ativo.cnpjFundo")
        }
        def ativoParaAtualizacao = operacao.ativo
        ativoParaAtualizacao.qtde = operacao.qtde
        ativoParaAtualizacao.valorTotalInvestido = operacao.valorTotalOperacao
        ativoParaAtualizacao.dataEntrada = operacao.data

        ativoParaAtualizacao
    }

    List<Operacao> complementarOperacao(Operacao operacao, List<Ativo> ativos) {
        def operacoesObtidas = []
        def qtdeTotalAtivo = ativos.sum({ it -> it.qtde }) as BigDecimal
        if (operacao.qtde > qtdeTotalAtivo) {
            throw new OperacaoInvalidaException("não é permitido vender mais que o estoque disponível do ativo. Cnpj do Ativo: $operacao.ativo.cnpjFundo. qtde venda: $operacao.qtde. Qtde disponível: ${qtdeTotalAtivo}")
        }

        for (Ativo ativo:ativos){
            def novaOperacao = new Operacao(
                    notaInvestimento: operacao.notaInvestimento,
                    tipoOperacao: operacao.tipoOperacao,
                    data: operacao.data,
                    ativo: ativo,
                    custoMedioOperacao: ativo.obterCustoMedioUnitario(),
            )
            if(ativo.qtde >= operacao.qtde) {
                novaOperacao.qtde = operacao.qtde
                novaOperacao.valorTotalOperacao = operacao.valorTotalOperacao
                novaOperacao.custoMedioOperacao = ativo.obterCustoMedioUnitario()
                novaOperacao.resultadoVenda = ativo.obterResultadoVenda(ativo.obterCustoMedioUnitario(), operacao.valorTotalOperacao, operacao.qtde)

                operacoesObtidas << novaOperacao
                break
            }
            else {
                def valorTotalProporcional = operacao.valorTotalOperacao.divide(operacao.qtde, 8, RoundingMode.HALF_UP) * ativo.qtde
                novaOperacao.qtde = ativo.qtde
                novaOperacao.valorTotalOperacao = valorTotalProporcional
                novaOperacao.custoMedioOperacao = ativo.obterCustoMedioUnitario()
                novaOperacao.resultadoVenda = ativo.obterResultadoVenda(ativo.obterCustoMedioUnitario(), valorTotalProporcional, ativo.qtde)

                operacoesObtidas << novaOperacao
                operacao.qtde-=ativo.qtde
                operacao.valorTotalOperacao-=valorTotalProporcional
            }
        }

        operacoesObtidas
    }

    NotaInvestimento obterDadosNotaInvestimento(List<String[]> linhasArquivoNota) {
        def dateFormatter = DateTimeFormatter.ofPattern('dd/MM/yyyy')

        new NotaInvestimento(
                dataMovimentacao: LocalDate.parse(linhasArquivoNota[1][1], dateFormatter),
                cnpjCorretora: linhasArquivoNota[2][1],
                nomeCorretora: linhasArquivoNota[3][1],
                regimeResgate: linhasArquivoNota[4][1]
        )
    }

    List<Operacao> incluiOperacoesComeCotas(String cnpjFundo, LocalDate dataRealizacaoComeCotas, List<OperacaoComeCotasDTO> operacoesComeCotasDTO) {
        List<Ativo> ativos = ativoRepository.getAllByCnpjFundoDatasEntrada(cnpjFundo, operacoesComeCotasDTO.dataAplicacao)
        List<Operacao> operacoesCriadas = []
        operacoesComeCotasDTO.each {operacaoComeCotasDTO ->
            def ativoParaAtualizar = ativos.find({it.dataEntrada == operacaoComeCotasDTO.dataAplicacao})
            def operacao = new Operacao(
                    tipoOperacao: TipoOperacaoEnum.i,
                    ativo: ativoParaAtualizar,
                    qtde: operacaoComeCotasDTO.qtdeComeCotas,
                    valorTotalOperacao: ativoParaAtualizar.obterCustoMedioUnitario() * operacaoComeCotasDTO.qtdeComeCotas,
                    custoMedioOperacao: ativoParaAtualizar.obterCustoMedioUnitario(),
                    data: dataRealizacaoComeCotas,
                    resultadoVenda: 0
            )
            operacaoRepository.incluir(operacao)
            ativoParaAtualizar.valorTotalInvestido-=operacao.valorTotalOperacao
            ativoParaAtualizar.qtde-=operacao.qtde
            ativoRepository.atualizar(ativoParaAtualizar)
            operacoesCriadas << operacao
        }
        operacoesCriadas
    }

}

package br.com.carteira.service.serviceComponents

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.Operacao
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.exception.ArquivoInvalidoException
import br.com.carteira.repository.AtivoRepository
import br.com.carteira.repository.OperacaoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.math.RoundingMode
import java.time.LocalDate

@Component
class FundosInvestimentosComponentService implements ComponentServiceTrait{
    AtivoRepository ativoRepository
    OperacaoRepository operacaoRepository

    @Autowired
    FundosInvestimentosComponentService(AtivoRepository ativoRepository, OperacaoRepository operacaoRepository) {
        this.ativoRepository = ativoRepository
        this.operacaoRepository = operacaoRepository
    }

    void incluiOperacao(String[] linhaArquivo, int numeroLinha, long idNotaInvestimento, LocalDate dataOperacao) {
        def operacao

        if (numeroLinha == 0) {
            if (linhaArquivo[0] != 'tipo')
                throw new ArquivoInvalidoException('Arquivo precisa possuir cabeçalhos de coluna conforme template')
        } else {
            def qtde = new BigDecimal(linhaArquivo[4].replace(",", "."))
            def valorTotalOperacao = new BigDecimal(linhaArquivo[3].replace(",", "."))
            def custoMedioOperacao = valorTotalOperacao.divide(qtde, 8, RoundingMode.HALF_UP)
            operacao = new Operacao(
                    data: dataOperacao,
                    idNotaInvestimento: idNotaInvestimento,
                    tipoOperacao: linhaArquivo[0],
                    ativo: Ativo.getInstanceWithAtributeMap(
                            nome: linhaArquivo[2],
                            tipo: TipoAtivoEnum.fiv,
                            cnpjFundo: linhaArquivo[1]
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
    Operacao incluir(Operacao operacao) {
        def ativo = ativoRepository.getByCnpjFundo(operacao.ativo.cnpjFundo)
        def operacaoAtualizada
        if (ativo != null) {
            operacao.ativo = ativo
            operacaoAtualizada = complementarOperacao(operacao)
            Ativo ativoParaAtualizacao = operacao.ativo.atualizarTituloAPartirDaOperacao(operacao)
            ativoRepository.atualizar(ativoParaAtualizacao)
        } else {
            operacao.ativo = criarAtivoAPartirDaOperacao(operacao)
            operacaoAtualizada = complementarOperacao(operacao)
            operacaoAtualizada.ativo.id = ativoRepository.incluir(operacao.ativo)
        }
        operacaoRepository.incluir(operacaoAtualizada)

        operacao
    }

}

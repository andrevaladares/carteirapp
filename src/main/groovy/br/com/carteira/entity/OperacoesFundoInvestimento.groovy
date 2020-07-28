package br.com.carteira.entity

import br.com.carteira.exception.AtivoInvalidoException
import br.com.carteira.exception.OperacaoInvalidaException
import org.apache.commons.lang3.StringUtils

class OperacoesFundoInvestimento implements OperacoesAtivo {

    def validar(Ativo ativo) {
        if(ativo.cnpjFundo == null) {
            throw new AtivoInvalidoException("é obrigatório informar o cnpj para ativos do tipo: $ativo.tipo")
        }

        if(ativo.cnpjFundo.length() != 14) {
            throw new AtivoInvalidoException("o cnpj deve possuir 14 caracteres. CNPJ errado: $ativo.cnpjFundo")
        }

        if(!StringUtils.isNumeric(ativo.cnpjFundo)) {
            throw new AtivoInvalidoException("o cnpj deve possuir apenas caracteres numéricos. CNPJ errado: $ativo.cnpjFundo")
        }
    }

    GString obterQueryUpdate(Ativo ativo) {
        def query =  """update ativo set nome = $ativo.nome, tipo = ${ativo.tipo as String},
            setor = $ativo.setor, qtde = $ativo.qtde, valor_total_investido = $ativo.valorTotalInvestido,
            data_entrada = $ativo.dataEntrada, cnpj_fundo = $ativo.cnpjFundo"""


        if(ativo.tipo in TipoAtivoEnum.getTesouro()) {
            query += """ where nome = $ativo.nome"""
        }
        else if(ativo.tipo == TipoAtivoEnum.fiv){
            query += """ where cnpj_fundo = $ativo.cnpjFundo"""
        }
        else {
            throw new OperacaoInvalidaException("nesse ponto a operacao de update só pode ocorrer para fundo de investimento ou tesouro direto")
        }
        query += """ and data_entrada = $ativo.dataEntrada and tipo = ${ativo.tipo as String}"""

        query
    }
}

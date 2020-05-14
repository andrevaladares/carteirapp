package br.com.carteira.entity

import br.com.carteira.exception.AtivoInvalidoException
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
        """update ativo set nome = $ativo.nome, tipo = ${ativo.tipo as String},
            setor = $ativo.setor, qtde = $ativo.qtde, valor_total_investido = $ativo.valorTotalInvestido,
            data_entrada = $ativo.dataEntrada, cnpj_fundo = $ativo.cnpjFundo where cnpj_fundo = $ativo.cnpjFundo 
        """
    }
}

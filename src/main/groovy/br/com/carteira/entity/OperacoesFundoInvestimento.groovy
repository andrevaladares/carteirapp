package br.com.carteira.entity

import br.com.carteira.exception.AtivoInvalidoException

class OperacoesFundoInvestimento implements OperacoesAtivo {

    def validar(Ativo ativo) {
        if(ativo.cnpjFundo == null) {
            throw new AtivoInvalidoException("é obrigatório informar o cnpj para ativos do tipo: $ativo.tipo")
        }

        if(ativo.cnpjFundo.length() != 14) {
            throw new AtivoInvalidoException("o cnpj deve possuir 14 caracteres. CNPJ errado: $ativo.cnpjFundo")
        }
    }

}

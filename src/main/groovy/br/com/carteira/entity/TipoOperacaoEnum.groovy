package br.com.carteira.entity

/**
 * Tipos de operação com ativos
 * v (venda / resgate)
 * c (compra / aplicação)
 * i (imposto / come cotas)
 * ts (transferencia de saída. Usado para debitar o saldo de dolares para compra de ações US)
 * te (transferencia de entrada. Usado para creditar o saldo de dolares resultante de operações com ações US)
 * div (dividendo. Trata-se de uma entrada de recursos originária de um ativo)
 * tx (taxa. Trata-se de uma sa~ida de recursos originária de um ativo)
 * d (desdobramento de ação)
 * a (agrupamento de ação)
 * j (juros de tesouro direto)
 */
enum TipoOperacaoEnum {
    v,c,i,ts,te,div,tx,d,a,j

}
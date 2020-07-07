package br.com.carteira.entity

/**
 * Tipos de operação com ativos
 * v (venda / resgate)
 * c (compra / aplicação)
 * i (imposto / come cotas)
 * ts (transferencia de saída. Usado para debitar o saldo de dolares para compra de ações US)
 * te (transferencia de entrada. Usado para creditar o saldo de dolares resultante de operações com ações US)
 */
enum TipoOperacaoEnum {
    v,c,i,ts,te

}
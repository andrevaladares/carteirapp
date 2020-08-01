package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum

class RegrasImpostosOuro implements RegrasImpostos {
    TipoAtivoEnum tipoAtivo = TipoAtivoEnum.oz2
    BigDecimal percentualImposto = 0.15
}

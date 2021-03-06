package br.com.carteira.entity

class NotaNegociacao {
    Long id
    BigDecimal taxaLiquidacao
    BigDecimal emolumentos
    BigDecimal taxaOperacional
    BigDecimal impostos
    BigDecimal irpfVendas
    BigDecimal outrosCustos
    BigDecimal taxaRegistroBmf
    BigDecimal taxasBmfEmolFgar
    BigDecimal valorDolarNaData

    BigDecimal getTotalTaxas() {
        taxaLiquidacao
                .add(emolumentos)
                .add(taxaOperacional)
                .add(impostos)
                .add(irpfVendas)
                .add(outrosCustos)
                .add(taxaRegistroBmf)
                .add(taxasBmfEmolFgar)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        NotaNegociacao that = (NotaNegociacao) o

        if (emolumentos != that.emolumentos) return false
        if (id != that.id) return false
        if (impostos != that.impostos) return false
        if (irpfVendas != that.irpfVendas) return false
        if (outrosCustos != that.outrosCustos) return false
        if (taxaLiquidacao != that.taxaLiquidacao) return false
        if (taxaOperacional != that.taxaOperacional) return false
        if (taxaRegistroBmf != that.taxaRegistroBmf) return false
        if (taxasBmfEmolFgar != that.taxasBmfEmolFgar) return false
        if (valorDolarNaData != that.valorDolarNaData) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (taxaLiquidacao != null ? taxaLiquidacao.hashCode() : 0)
        result = 31 * result + (emolumentos != null ? emolumentos.hashCode() : 0)
        result = 31 * result + (taxaOperacional != null ? taxaOperacional.hashCode() : 0)
        result = 31 * result + (impostos != null ? impostos.hashCode() : 0)
        result = 31 * result + (irpfVendas != null ? irpfVendas.hashCode() : 0)
        result = 31 * result + (outrosCustos != null ? outrosCustos.hashCode() : 0)
        result = 31 * result + (taxaRegistroBmf != null ? taxaRegistroBmf.hashCode() : 0)
        result = 31 * result + (taxasBmfEmolFgar != null ? taxasBmfEmolFgar.hashCode() : 0)
        result = 31 * result + (valorDolarNaData != null ? valorDolarNaData.hashCode() : 0)
        return result
    }
}

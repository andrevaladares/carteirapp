package br.com.carteira.repository

import br.com.carteira.entity.NotaNegociacao
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Connection

@Repository
class NotaNegociacaoRepository {

    DriverManagerDataSource dataSource

    @Autowired
    NotaNegociacaoRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    Long incluir(NotaNegociacao notaNegociacao) {
        def keys
        def insertSql = """
                insert into nota_negociacao (taxa_liquidacao, emolumentos, 
                taxa_operacional, impostos, irpf_vendas, outros_custos_oper, taxa_registro_bmf,
                taxas_bmf_emol_fgar)
                values ($notaNegociacao.taxaLiquidacao, $notaNegociacao.emolumentos, 
                        $notaNegociacao.taxaOperacional, $notaNegociacao.impostos, 
                        $notaNegociacao.irpfVendas, $notaNegociacao.outrosCustos,
                        $notaNegociacao.taxaRegistroBmf, $notaNegociacao.taxasBmfEmolFgar)
            """

        Connection conn = DataSourceUtils.getConnection(dataSource)
        keys  = new Sql(conn).executeInsert(insertSql)
        keys[0][0] as Long
    }

    List listAll() {
        List resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows('select * from nota_negociacao')
        return resultado
    }

    NotaNegociacao fromNotaNegociacaoGroovyRow(GroovyRowResult notaNegociacaoGroovyRow) {
        def notaNegociacao = null
        if (notaNegociacaoGroovyRow != null) {
            notaNegociacao = new NotaNegociacao(
                    id: notaNegociacaoGroovyRow['id'],
                    taxaLiquidacao: notaNegociacaoGroovyRow['taxa_liquidacao'],
                    emolumentos: notaNegociacaoGroovyRow['emolumentos'],
                    taxaOperacional: notaNegociacaoGroovyRow['taxa_operacional'],
                    impostos: notaNegociacaoGroovyRow['impostos'],
                    irpfVendas: notaNegociacaoGroovyRow['irpf_vendas'],
                    outrosCustos: notaNegociacaoGroovyRow['outros_custos_oper']
            )
        }
        notaNegociacao
    }
}

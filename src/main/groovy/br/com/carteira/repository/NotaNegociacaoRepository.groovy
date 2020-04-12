package br.com.carteira.repository

import br.com.carteira.entity.NotaNegociacao
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import javax.sql.DataSource
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
                taxa_operacional, impostos, irpf_vendas, outros_custos_oper)
                values ($notaNegociacao.taxaLiquidacao, $notaNegociacao.emolumentos, 
                        $notaNegociacao.taxaOperacional, $notaNegociacao.impostos, 
                        $notaNegociacao.irpfVendas, $notaNegociacao.outrosCustos)
            """

        Connection conn = DataSourceUtils.getConnection(dataSource)
        keys  = new Sql(conn).executeInsert(insertSql)
        keys[0][0] as Long
    }
}

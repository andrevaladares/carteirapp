package br.com.carteira.repository

import br.com.carteira.entity.NotaInvestimento
import br.com.carteira.entity.NotaNegociacao
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Connection
import java.sql.Date

@Repository
class NotaInvestimentoRepository {

    DriverManagerDataSource dataSource

    @Autowired
    NotaInvestimentoRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    Long incluir(NotaInvestimento notaInvestimento) {
        def keys
        def insertSql = """
                insert into nota_investimento (data_movimentacao, cnpj_corretora, 
                nome_corretora, regime_resgate)
                values ($notaInvestimento.dataMovimentacao, $notaInvestimento.cnpjCorretora, 
                        $notaInvestimento.nomeCorretora, ${notaInvestimento.regimeResgate as String})
            """

        Connection conn = DataSourceUtils.getConnection(dataSource)
        keys  = new Sql(conn).executeInsert(insertSql)
        keys[0][0] as Long
    }

    NotaInvestimento getById(Long id) {
        def sql = "select * from nota_investimento where id = :idNota"
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['idNota': id], sql)

        fromNotaInvestimentoGroovyRow(resultado)
    }

    List listAll() {
        List resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows('select * from nota_investimento')
        return resultado
    }

    NotaInvestimento fromNotaInvestimentoGroovyRow(GroovyRowResult notaInvestimentoGroovyRow) {
        def notaInvestimento = null
        if (notaInvestimentoGroovyRow != null) {
            notaInvestimento = new NotaInvestimento(
                    id: notaInvestimentoGroovyRow['id'],
                    dataMovimentacao: ((Date) notaInvestimentoGroovyRow['data_movimentacao']).toLocalDate(),
                    cnpjCorretora: notaInvestimentoGroovyRow['cnpj_corretora'],
                    nomeCorretora: notaInvestimentoGroovyRow['nome_corretora'],
                    regimeResgate: notaInvestimentoGroovyRow['regime_resgate']
            )
        }
        notaInvestimento
    }
}

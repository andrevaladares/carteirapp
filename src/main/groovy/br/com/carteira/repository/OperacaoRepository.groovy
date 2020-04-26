package br.com.carteira.repository

import br.com.carteira.entity.Operacao
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Connection
import java.time.LocalDate

@Repository
class OperacaoRepository {

    DriverManagerDataSource dataSource

    @Autowired
    OperacaoRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    Long incluir(Operacao operacao) {
        def keys
        def insertSql = """
                insert into operacao (nota_negociacao, tipo_operacao, ativo, qtde,
                    valor_total_operacao, custo_medio_venda, resultado_venda, data)
                values (${operacao.idNotaNegociacao}, ${operacao.tipoOperacao as String}, ${operacao.ativo.id}, 
                    ${operacao.qtde}, ${operacao.valorTotalOperacao}, ${operacao.custoMedioVenda}, ${operacao.resultadoVenda},
                    ${operacao.data})
            """

        Connection conn = DataSourceUtils.getConnection(dataSource)
        keys  = new Sql(conn).executeInsert(insertSql)
        keys[0][0] as Long

    }

    GroovyRowResult getById(Long idOperacao) {
        def query = 'select * from operacao where id = :idOperacao'
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['idOperacao': idOperacao], query)
        return resultado
    }

    List<GroovyRowResult> findAll(){
        def query = 'select * from operacao'
        new Sql(DataSourceUtils.getConnection(dataSource)).rows(query)
    }

    List<GroovyRowResult> getByDataOperacaoTicker(LocalDate dataOperacao, String ticker) {
        def query = """
            select * from operacao o inner join ativo a on a.id =  o.ativo
            where a.ticker = $ticker and o.data = $dataOperacao
        """
        new Sql(DataSourceUtils.getConnection(dataSource)).rows(query)
    }
}

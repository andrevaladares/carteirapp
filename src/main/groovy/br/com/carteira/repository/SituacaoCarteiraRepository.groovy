package br.com.carteira.repository

import br.com.carteira.entity.SituacaoCarteira
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Date
import java.time.LocalDate

@Repository
class SituacaoCarteiraRepository {

    DriverManagerDataSource dataSource

    @Autowired
    SituacaoCarteiraRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    Long incluir(SituacaoCarteira situacaoCarteira) {
        def insertSql = """
                insert into situacao_carteira (data, titulo, qtde_disponivel, valor_investido, valor_atual)
                values ($situacaoCarteira.data, $situacaoCarteira.idTitulo, $situacaoCarteira.qtdeDisponivel, 
                    $situacaoCarteira.valorInvestido, $situacaoCarteira.valorAtual)
            """
        def keys = new Sql(DataSourceUtils.getConnection(dataSource)).executeInsert insertSql

        keys[0][0] as Long
    }

    SituacaoCarteira getByTickerDataReferencia(String ticker, LocalDate dataReferencia) {
        def sql = 'select * from situacao_carteira where titulo = (select id from titulo where ticker = :ticker) and data = :dataReferencia'

        def resultado =  new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['ticker': ticker.toLowerCase(), 'dataReferencia': dataReferencia], sql)

        situacaoCarteiraFromGroovyRowResult(resultado)
    }

    SituacaoCarteira situacaoCarteiraFromGroovyRowResult(GroovyRowResult groovyRowResult) {
        new SituacaoCarteira(
                id: groovyRowResult['id'],
                data: ((Date)groovyRowResult['data']).toLocalDate(),
                idTitulo: groovyRowResult['titulo'],
                qtdeDisponivel: groovyRowResult['qtde_disponivel'],
                valorInvestido: groovyRowResult['valor_investido'],
                valorAtual: groovyRowResult['valor_atual']
        )
    }

    List<GroovyRowResult> listaTodosPorDataReferencia(LocalDate dataReferencia) {
        def sql = "select * from situacao_carteira sc inner join titulo t on sc.titulo = t.id where sc.data = :dataReferencia"

        new Sql(DataSourceUtils.getConnection(dataSource)).rows(['dataReferencia': dataReferencia], sql)
    }
}
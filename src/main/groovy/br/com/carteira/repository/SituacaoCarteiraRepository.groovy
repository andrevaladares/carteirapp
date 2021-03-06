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
                insert into situacao_carteira (data, ativo, qtde_disponivel, valor_investido, 
                    valor_investido_dolares, valor_atual, valor_atual_dolares)
                values ($situacaoCarteira.data, $situacaoCarteira.idAtivo, $situacaoCarteira.qtdeDisponivel, 
                    $situacaoCarteira.valorInvestido, $situacaoCarteira.valorInvestidoDolares, 
                    $situacaoCarteira.valorAtual, $situacaoCarteira.valorAtualDolares)
            """
        def keys = new Sql(DataSourceUtils.getConnection(dataSource)).executeInsert insertSql

        keys[0][0] as Long
    }

    SituacaoCarteira getByDataReferenciaIdAtivo(Long idAtivo, LocalDate dataReferencia) {
        def sql = """
                select * 
                from situacao_carteira 
                where ativo = :ativo and data = :dataReferencia
            """

        def resultado =  new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['ativo': idAtivo, 'dataReferencia': dataReferencia], sql)

        situacaoCarteiraFromGroovyRowResult(resultado)
    }

    SituacaoCarteira situacaoCarteiraFromGroovyRowResult(GroovyRowResult groovyRowResult) {
        if(!groovyRowResult) return null

        new SituacaoCarteira(
                id: groovyRowResult['id'],
                data: ((Date)groovyRowResult['data']).toLocalDate(),
                idAtivo: groovyRowResult['ativo'],
                qtdeDisponivel: groovyRowResult['qtde_disponivel'],
                valorInvestido: groovyRowResult['valor_investido'],
                valorInvestidoDolares: groovyRowResult['valor_investido_dolares'],
                valorAtual: groovyRowResult['valor_atual'],
                valorAtualDolares: groovyRowResult['valor_atual_dolares']
        )
    }

    List<GroovyRowResult> listaTodosPorDataReferenciaComExcecaoDe(LocalDate dataReferencia, String[] booksAExcluir) {
        def booksAExcluirMapa = montaMapaStrings(booksAExcluir)
        def sql = """
                        select * from situacao_carteira sc inner join ativo a on sc.ativo = a.id 
                        where sc.data = :dataReferencia
                  """
        if(booksAExcluirMapa) {
            sql += """
                        and (a.book is null or a.book not in (${booksAExcluirMapa.keySet().collect{":$it"}.join(',')}))
                  """
        }
        sql += """
                        order by a.book, a.tipo, a.ticker, a.nome, a.data_entrada
               """

        def mapaParametros = ['dataReferencia': dataReferencia]
        mapaParametros.putAll(booksAExcluirMapa)

        new Sql(DataSourceUtils.getConnection(dataSource)).rows(sql, mapaParametros)
    }

    Map montaMapaStrings(String[] strings) {
        def contador = 0
        strings.collectEntries({
            contador++
            ['str' + contador, it]
        })
    }
}

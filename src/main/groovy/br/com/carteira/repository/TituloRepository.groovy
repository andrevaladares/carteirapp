package br.com.carteira.repository

import br.com.carteira.entity.TipoTituloEnum
import br.com.carteira.entity.Titulo
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class TituloRepository {

    DriverManagerDataSource dataSource

    @Autowired
    OperacaoRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    List listAll() {
        List resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows('select * from titulo')
        return resultado
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    Long incluir(Titulo titulo) {

        def insertSql = """
                insert into titulo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
                values (${titulo.ticker}, $titulo.nome, ${titulo.tipo as String}, 
                    $titulo.setor, $titulo.qtde, $titulo.valorTotalInvestido, ${titulo.dataEntrada})
            """
        def keys = new Sql(DataSourceUtils.getConnection(dataSource)).executeInsert insertSql

        keys[0][0] as Long
    }

    Titulo getById(Long idTitulo) {
        def query = 'select * from titulo where id = :idTitulo'
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['idTitulo': idTitulo], query)
        return fromTituloGroovyRow(resultado)
    }

    Titulo getByTicker(String ticker) {
        def query = 'select * from titulo where ticker = :ticker'
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['ticker': ticker], query)
        return fromTituloGroovyRow(resultado)
    }

    Long atualizar(Titulo titulo) {
        def updateQuery = """update titulo set nome = $titulo.nome, tipo = ${titulo.tipo as String},
            setor = $titulo.setor, qtde = $titulo.qtde, valor_total_investido = $titulo.valorTotalInvestido,
            data_entrada = $titulo.dataEntrada where ticker = $titulo.ticker 
        """

        new Sql(DataSourceUtils.getConnection(dataSource)).executeUpdate(updateQuery)
    }

    Titulo fromTituloGroovyRow(GroovyRowResult tituloGroovyRow) {
        def titulo
        if (tituloGroovyRow != null) {
            titulo = new Titulo(
                    id: tituloGroovyRow['id'],
                    ticker: tituloGroovyRow['ticker'],
                    nome: tituloGroovyRow['nome'],
                    tipo: tituloGroovyRow['tipo'] as TipoTituloEnum,
                    setor: tituloGroovyRow['setor'],
                    qtde: tituloGroovyRow['qtde'],
                    valorTotalInvestido: tituloGroovyRow['valor_total_investido'],
                    dataEntrada: ((Date) tituloGroovyRow['data_entrada']).toLocalDate()
            )
        }
        titulo
    }
}

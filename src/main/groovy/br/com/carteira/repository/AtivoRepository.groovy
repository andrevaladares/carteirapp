package br.com.carteira.repository

import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.Ativo
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Date

@Repository
class AtivoRepository {

    DriverManagerDataSource dataSource

    @Autowired
    AtivoRepository(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    List listAll() {
        List resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows('select * from ativo')
        return resultado
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    Long incluir(Ativo ativo) {

        def insertSql = """
                insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada)
                values (${ativo.ticker.toLowerCase()}, $ativo.nome, ${ativo.tipo as String}, 
                    $ativo.setor, $ativo.qtde, $ativo.valorTotalInvestido, ${ativo.dataEntrada})
            """
        def keys = new Sql(DataSourceUtils.getConnection(dataSource)).executeInsert insertSql

        keys[0][0] as Long
    }

    Ativo getById(Long idAtivo) {
        def query = 'select * from ativo where id = :idAtivo'
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['idAtivo': idAtivo], query)
        return fromAtivoGroovyRow(resultado)
    }

    Ativo getByTicker(String ticker) {
        def query = 'select * from ativo where ticker = :ticker'
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['ticker': ticker.toLowerCase()], query)
        return fromAtivoGroovyRow(resultado)
    }

    Long atualizar(Ativo ativo) {
        def updateQuery = """update ativo set nome = $ativo.nome, tipo = ${ativo.tipo as String},
            setor = $ativo.setor, qtde = $ativo.qtde, valor_total_investido = $ativo.valorTotalInvestido,
            data_entrada = $ativo.dataEntrada where ticker = $ativo.ticker 
        """

        new Sql(DataSourceUtils.getConnection(dataSource)).executeUpdate(updateQuery)
    }

    Ativo fromAtivoGroovyRow(GroovyRowResult ativoGroovyRow) {
        def ativo = null
        if (ativoGroovyRow != null) {
            ativo = new Ativo(
                    id: ativoGroovyRow['id'],
                    ticker: ativoGroovyRow['ticker'],
                    nome: ativoGroovyRow['nome'],
                    tipo: ativoGroovyRow['tipo'] as TipoAtivoEnum,
                    setor: ativoGroovyRow['setor'],
                    qtde: ativoGroovyRow['qtde'],
                    valorTotalInvestido: ativoGroovyRow['valor_total_investido'],
                    dataEntrada: ((Date) ativoGroovyRow['data_entrada']).toLocalDate()
            )
        }
        ativo
    }
}

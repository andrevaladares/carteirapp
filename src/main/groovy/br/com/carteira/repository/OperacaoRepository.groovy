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
                    valor_total_operacao, custo_medio_operacao, resultado_venda, data, nota_investimento)
                values ($operacao.idNotaNegociacao, ${operacao.tipoOperacao as String}, $operacao.ativo.id, 
                    $operacao.qtde, $operacao.valorTotalOperacao, $operacao.custoMedioOperacao, $operacao.resultadoVenda,
                    $operacao.data, ${operacao.notaInvestimento?.id})
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

    List<GroovyRowResult> getByDataOperacaoCnpjFundo(LocalDate dataOperacao, String cnpjFundo) {
        def query = """
            select o.tipo_operacao, o.ativo, o.qtde, o.valor_total_operacao,
                o.custo_medio_operacao, o.resultado_venda 
            from operacao o inner join ativo a on a.id =  o.ativo
            where a.cnpj_fundo = $cnpjFundo and o.data = $dataOperacao
        """
        new Sql(DataSourceUtils.getConnection(dataSource)).rows(query)
    }

    List<GroovyRowResult> getByDataOperacaoNomeAtivo(LocalDate dataOperacao, String nomeAtivo) {
        def query = """
            select o.tipo_operacao, o.ativo, o.qtde, o.valor_total_operacao,
                o.custo_medio_operacao, o.resultado_venda 
            from operacao o inner join ativo a on a.id =  o.ativo
            where a.nome = $nomeAtivo and o.data = $dataOperacao
        """
        new Sql(DataSourceUtils.getConnection(dataSource)).rows(query)
    }
}

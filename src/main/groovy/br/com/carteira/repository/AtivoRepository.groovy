package br.com.carteira.repository

import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.entity.Ativo
import br.com.carteira.exception.CunsultaInvalidaException
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Repository

import java.sql.Date
import java.time.LocalDate

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
        ativo.ticker = ativo.ticker ? ativo.ticker.toLowerCase() : null
        def insertSql = """
                insert into ativo (ticker, nome, tipo, setor, qtde, valor_total_investido, data_entrada, cnpj_fundo)
                values ($ativo.ticker, $ativo.nome, ${ativo.tipo as String}, 
                    $ativo.setor, $ativo.qtde, $ativo.valorTotalInvestido, ${ativo.dataEntrada},
                    $ativo.cnpjFundo)
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

    /**
     * Retorna todas as ocorrências do ativo indicado pelo cnpj cuja quantidade seja maior que zero
     *
     * @param cnpjFundo o cnpj do ativo (geralmente fundo de investimento)
     * @param ordenacao do resultado
     * @return lista dos ativos que correspondem ao cnpj informado com quantidade maior que zero ordenados por data de entrada
     */
    List<Ativo> getAllByCnpjFundo(String cnpjFundo, String ordenacao) {
        def query = "select * from ativo where cnpj_fundo = :cnpjFundo and qtde > 0 order by data_entrada $ordenacao"
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows(['cnpjFundo': cnpjFundo], query)

        return resultado.collect({fromAtivoGroovyRow(it)})
    }

    /**
     * Retorna todas as ocorrências do ativo cujos campos ticker,
     * cnpj fundo ou nome coincidam com o exemplo e com quantidade maior que 0
     *
     * @param ativoExemplo o ativo com as informações de critério preenchidas
     * @param ordenacao do resultado
     * @return lista dos ativos cujo ticker, cnpjFundo ou nome coincidam com o exemplo
     */
    List<Ativo> getAllByAtivoExample(Ativo ativoExemplo, String ordenacao) {
        StringBuilder queryStrBuilder = new StringBuilder("select * from ativo where ")
        def paramsMap = []
        if(ativoExemplo.ticker) {
            queryStrBuilder.append("ticker = :ticker")
            paramsMap << ['ticker': ativoExemplo.ticker]
        }
        else if(ativoExemplo.cnpjFundo) {
            queryStrBuilder.append("cnpj_fundo = :cnpjFundo")
            paramsMap << ['cnpjFundo': ativoExemplo.cnpjFundo]
        }
        else if(ativoExemplo.nome) {
            queryStrBuilder.append("nome = :nome")
            paramsMap << ['nome': ativoExemplo.nome]
        }
        else {
            throw new CunsultaInvalidaException("ao menos um dos critérios precisa ser informado (ticker, cnpjFundo ou nome")
        }
        queryStrBuilder.append(" and qtde > 0 order by data_entrada $ordenacao")
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows(queryStrBuilder.toString(), paramsMap)

        return resultado.collect({fromAtivoGroovyRow(it)})
    }

    List<Ativo> getAllByCnpjFundoDatasEntrada(String cnpjFundo, ArrayList<LocalDate> datasAplicacoes) {
        def mapaDatas = montaMapaDatas(datasAplicacoes)

        def query = "select * from ativo where cnpj_fundo = :cnpjFundo and data_entrada in (${mapaDatas.keySet().collect{":$it"}.join(',')}) and qtde > 0 order by data_entrada"
        Map<String, Object> mapaParametros = ['cnpjFundo': cnpjFundo]
        mapaParametros.putAll(mapaDatas)
        def resultado = new Sql(DataSourceUtils.getConnection(dataSource)).rows(query, mapaParametros)

        return resultado.collect({fromAtivoGroovyRow(it)})
    }

    Long atualizar(Ativo ativo) {
        def updateQuery = ativo.obterQueryUpdate()

        new Sql(DataSourceUtils.getConnection(dataSource)).executeUpdate(updateQuery)
    }

    Ativo fromAtivoGroovyRow(GroovyRowResult ativoGroovyRow) {
        def ativo = null
        if (ativoGroovyRow != null) {
            ativo = Ativo.getInstanceWithAtributeMap(
                    id: ativoGroovyRow['id'],
                    ticker: ativoGroovyRow['ticker'],
                    nome: ativoGroovyRow['nome'],
                    tipo: ativoGroovyRow['tipo'] as TipoAtivoEnum,
                    setor: ativoGroovyRow['setor'],
                    qtde: ativoGroovyRow['qtde'],
                    valorTotalInvestido: ativoGroovyRow['valor_total_investido'],
                    dataEntrada: ((Date) ativoGroovyRow['data_entrada']).toLocalDate(),
                    cnpjFundo: ativoGroovyRow['cnpj_fundo']
            )
        }
        ativo
    }

    Map montaMapaDatas(List<LocalDate> datas) {
        def contador = 0
        datas.collectEntries({
            contador++
            ['data' + contador, it]
        })
    }
}

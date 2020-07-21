package br.com.carteira.impostos

import br.com.carteira.entity.TipoAtivoEnum
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.time.YearMonth

@Component
@Transactional(readOnly = true)
class ConsolidacaoImpostos {

    DriverManagerDataSource dataSource
    def regrasImpostos = [
            new RegrasImpostosAcoes(),
            new RegrasImpostosFIIs(),
            new RegrasImpostosFINs(),
            new RegrasImpostosAcoesUs()
    ]

    @Autowired
    ConsolidacaoImpostos(DriverManagerDataSource dataSource) {
        this.dataSource = dataSource
    }

    @Transactional
    void consolidarImpostos(YearMonth mesAno, TipoAtivoEnum tipoDeAtivo) {
        regrasImpostos.find({it.tipoAtivo == tipoDeAtivo}).consolidarImpostos(mesAno, tipoDeAtivo, dataSource)
    }

    GroovyRowResult obterConsolidacao(YearMonth mesAnoReferencia, TipoAtivoEnum tipoDeAtivo) {
        def query = '''
            select * from consolidacao_impostos_mes
            where data = :dataReferencia and tipo_ativo = :tipoAtivo
        '''
        new Sql(DataSourceUtils.getConnection(dataSource)).firstRow(['dataReferencia': mesAnoReferencia.atEndOfMonth(),
                                                                     'tipoAtivo'     : tipoDeAtivo as String], query)
    }
}

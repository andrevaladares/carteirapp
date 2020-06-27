package br.com.carteira.repository

import br.com.carteira.entity.ExportacaoSituacaoDTO
import br.com.carteira.entity.TipoAtivoEnum
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.time.LocalDate

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testContext.xml")
@Sql(scripts = ["classpath:limpaDados.sql",
        "classpath:ativosFundosInvestimento.sql",
        "classpath:ativosTesouroSelic.sql",
        "classpath:situacaoCarteiraConsolidacaoAtivos.sql"])
class SituacaoCarteiraRepositoryIT {

    @Autowired
    SituacaoCarteiraRepository situacaoCarteiraRepository

    @Test
    void "consolida corretamente os registros de situacao do mesmo ativo"(){
        def carteira = situacaoCarteiraRepository.listaTodosPorDataReferencia(LocalDate.of(2020, 2, 28))
        def valorTotalAtual = carteira.sum({ it -> it['valor_atual'] })
        carteira = carteira.collect {it ->
            TipoAtivoEnum tipoAtivoEnum = it['tipo']
            def identificador = tipoAtivoEnum.getIdEmSituacaoCarteira()
            def valorInvestido = it['valor_investido']
            def valorAtual = it['valor_atual']
            def alocacao = it['valor_atual'] / valorTotalAtual
            new ExportacaoSituacaoDTO(
                    ativo: it[identificador],
                    tipo: tipoAtivoEnum,
                    dataEntrada: it['data_entrada'].toLocalDate(),
                    dataSituacao: it['data'].toLocalDate(),
                    qtde: it['qtde_disponivel'],
                    valorInvestido: valorInvestido,
                    valorAtual: valorAtual,
                    alocacaoAtual: alocacao
            )
        }.groupBy {it['ativo']}
            .collectEntries {[(it.key): ['tipo': it.value['tipo'][0],
                    'dataEntrada': it.value.min {it['dataEntrada']}['dataEntrada'],
                    'dataSituacao': it.value['dataSituacao'][0],
                    'qtde': it.value.sum {it['qtde']},
                    'valorInvestido': it.value.sum {it['valorInvestido']},
                    'valorAtual': it.value.sum {it['valorAtual']},
                    'alocacaoAtual': it.value.sum {it['valorAtual']} / valorTotalAtual
            ]]}

        assert carteira['Votorantim FIC de FI Cambial']['tipo'] == TipoAtivoEnum.fiv
        assert carteira['Votorantim FIC de FI Cambial']['dataEntrada'] == LocalDate.of(2020, 3, 18)
        assert carteira['Votorantim FIC de FI Cambial']['dataSituacao'] == LocalDate.of(2020, 2, 28)
        assert carteira['Votorantim FIC de FI Cambial']['qtde'] == 9486.1505
        assert carteira['Votorantim FIC de FI Cambial']['valorInvestido'] ==69938.07
        assert carteira['Votorantim FIC de FI Cambial']['valorAtual'] == 70230.00
        assert carteira['Votorantim FIC de FI Cambial']['alocacaoAtual'] ==0.3503966472

        assert carteira['Tesouro Selic 2025']['tipo'] == TipoAtivoEnum.tse
        assert carteira['Tesouro Selic 2025']['dataEntrada'] == LocalDate.of(2020, 3, 16)
        assert carteira['Tesouro Selic 2025']['dataSituacao'] == LocalDate.of(2020, 2, 28)
        assert carteira['Tesouro Selic 2025']['qtde'] == 12.13
        assert carteira['Tesouro Selic 2025']['valorInvestido'] == 127981.20
        assert carteira['Tesouro Selic 2025']['valorAtual'] == 130200.00
        assert carteira['Tesouro Selic 2025']['alocacaoAtual'] == 0.6496033528

    }
}

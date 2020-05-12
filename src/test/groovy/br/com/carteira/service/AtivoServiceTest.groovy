package br.com.carteira.service

import br.com.carteira.entity.Ativo
import br.com.carteira.entity.TipoAtivoEnum
import br.com.carteira.exception.AtivoInvalidoException
import br.com.carteira.repository.AtivoRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class AtivoServiceTest {
    @Mock
    AtivoRepository tituloRepository
    @InjectMocks
    AtivoService ativoService

    @Test
    void incluirTituloSucesso() {
        def tituloAIncluir = Ativo.getInstanceWithAtributeMap(
                ticker: 'visc11',
                nome: 'Vinci Shoppping',
                tipo: 'fii'
        )
        Mockito.when(tituloRepository.incluir(tituloAIncluir)).thenReturn(1L)

        Assert.assertEquals(1, ativoService.incluir(tituloAIncluir))
    }

    @Test
    void 'falha ao tentar incluir ativo comum sem ticker'(){
        def ativo = Ativo.getInstanceWithAtributeMap(
                nome: 'Vinci Shoppping',
                tipo: TipoAtivoEnum.fii
        )
        try {
            ativoService.incluir(ativo)
            Assert.fail()
        }
        catch (AtivoInvalidoException e){
            assert e.getMessage() == "é obrigatório informar o ticker para ativos do tipo: $ativo.tipo"
        }
    }

    @Test
    void 'falha ao tentar incluir ouro sem ticker'(){
        def ativo = Ativo.getInstanceWithAtributeMap(
                nome: 'Ouro a vista',
                tipo: TipoAtivoEnum.oz2
        )
        try {
            ativoService.incluir(ativo)
            Assert.fail()
        }
        catch (AtivoInvalidoException e){
            assert e.getMessage() == "é obrigatório informar o ticker para ativos do tipo: $ativo.tipo"
        }
    }

    @Test
    void 'falha ao tentar incluir fundo de investimento sem cnpj'(){
        def ativo = Ativo.getInstanceWithAtributeMap(
                nome: 'Fundo de investimento cambial',
                tipo: TipoAtivoEnum.fiv
        )
        try {
            ativoService.incluir(ativo)
            Assert.fail()
        }
        catch (AtivoInvalidoException e){
            assert e.getMessage() == "é obrigatório informar o cnpj para ativos do tipo: $ativo.tipo"
        }
    }

    @Test
    void 'falha ao tentar incluir fundo de investimento com cnpj de tamanho invalido'(){
        def ativo = Ativo.getInstanceWithAtributeMap(
                nome: 'Fundo de investimento cambial',
                tipo: TipoAtivoEnum.fiv,
                cnpjFundo: '0521706500010745'
        )
        try {
            ativoService.incluir(ativo)
            Assert.fail()
        }
        catch (AtivoInvalidoException e){
            assert e.getMessage() == "o cnpj deve possuir 14 caracteres. CNPJ errado: $ativo.cnpjFundo"
        }
    }
}
package br.com.carteira.service

import br.com.carteira.entity.Titulo
import br.com.carteira.repository.TituloRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class TituloServiceTest {
    @Mock
    TituloRepository tituloRepository
    @InjectMocks
    TituloService tituloService

    @Test
    void incluirTituloSucesso() {
        def tituloAIncluir = new Titulo(
                ticker: 'visc11',
                nome: 'Vinci Shoppping',
                tipo: 'f'
        )
        Mockito.when(tituloRepository.incluir(tituloAIncluir)).thenReturn(1L)

        Assert.assertEquals(1, tituloService.incluir(tituloAIncluir))
    }
}


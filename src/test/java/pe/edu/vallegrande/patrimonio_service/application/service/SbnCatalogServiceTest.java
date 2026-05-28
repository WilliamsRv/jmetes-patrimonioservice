package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.SbnCatalogResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.SbnCatalogPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SbnCatalogService - Servicio de Catálogo SBN")
class SbnCatalogServiceTest {

    @Mock
    private SbnCatalogPersistencePort persistencePort;

    @InjectMocks
    private SbnCatalogService service;

    private SbnCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new SbnCatalog();
        catalog.setCodigo("51111001");
        catalog.setDescripcion("Escritorio de madera");
        catalog.setGrupo("MOBILIARIO");
        catalog.setClase("Muebles de Oficina");
        catalog.setTasaDepreciacionAnual(new BigDecimal("10"));
        catalog.setVidaUtilMeses(120);
        catalog.setEsDepreciable(true);
        catalog.setRequiereSerieMarcaModelo(false);
    }

    @Test
    @DisplayName("getAll: Retorna todos los registros del catálogo SBN")
    void getAll_ShouldReturnFlux() {
        when(persistencePort.findAll()).thenReturn(Flux.just(catalog));

        StepVerifier.create(service.getAll())
                .expectNextMatches(res -> "51111001".equals(res.getCodigo())
                        && "Escritorio de madera".equals(res.getDescripcion())
                        && "MOBILIARIO".equals(res.getGrupo()))
                .verifyComplete();
    }

    @Test
    @DisplayName("getByCodigo: Retorna un registro cuando el código existe")
    void getByCodigo_WhenExists_ShouldReturnResponse() {
        when(persistencePort.findByCodigo("51111001")).thenReturn(Mono.just(catalog));

        StepVerifier.create(service.getByCodigo("51111001"))
                .expectNextMatches(SbnCatalogResponse.class::isInstance)
                .verifyComplete();
    }

    @Test
    @DisplayName("getByCodigo: Retorna vacío cuando el código NO existe")
    void getByCodigo_WhenNotExists_ShouldReturnEmpty() {
        when(persistencePort.findByCodigo("NOEXISTE")).thenReturn(Mono.empty());

        StepVerifier.create(service.getByCodigo("NOEXISTE"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getGrupos: Retorna lista de grupos distintos del catálogo")
    void getGrupos_ShouldReturnDistinctGrupos() {
        when(persistencePort.findDistinctGrupos()).thenReturn(Flux.just("MOBILIARIO", "EQUIPOS DE COMPUTO"));

        StepVerifier.create(service.getGrupos())
                .expectNext("MOBILIARIO", "EQUIPOS DE COMPUTO")
                .verifyComplete();
    }

    @Test
    @DisplayName("getByGrupo: Retorna registros filtrados por grupo")
    void getByGrupo_ShouldReturnFlux() {
        when(persistencePort.findByGrupo("MOBILIARIO")).thenReturn(Flux.just(catalog));

        StepVerifier.create(service.getByGrupo("MOBILIARIO"))
                .expectNextMatches(res -> res.getCodigo().equals("51111001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("toResponse: Mapea todos los campos correctamente de SbnCatalog a SbnCatalogResponse")
    void toResponse_ShouldMapAllFields() {
        when(persistencePort.findAll()).thenReturn(Flux.just(catalog));

        StepVerifier.create(service.getAll())
                .assertNext(res -> {
                    assertEquals("51111001", res.getCodigo());
                    assertEquals("Escritorio de madera", res.getDescripcion());
                    assertEquals("MOBILIARIO", res.getGrupo());
                    assertEquals("Muebles de Oficina", res.getClase());
                    assertEquals(0, res.getTasaDepreciacionAnual().compareTo(new BigDecimal("10")));
                    assertEquals(120, res.getVidaUtilMeses());
                    assertEquals(true, res.getEsDepreciable());
                    assertEquals(false, res.getRequiereSerieMarcaModelo());
                })
                .verifyComplete();
    }
}

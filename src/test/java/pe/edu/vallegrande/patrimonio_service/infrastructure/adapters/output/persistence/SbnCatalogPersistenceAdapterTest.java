package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.SbnCatalogRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SbnCatalogPersistenceAdapter - Adaptador de persistencia del Catálogo SBN")
class SbnCatalogPersistenceAdapterTest {

    @Mock
    private SbnCatalogRepository repository;

    @InjectMocks
    private SbnCatalogPersistenceAdapter adapter;

    private SbnCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new SbnCatalog();
        catalog.setId(UUID.randomUUID());
        catalog.setCodigo("51111001");
    }

    @Test
    @DisplayName("findAll: Retorna todos los registros del catálogo desde el repositorio")
    void findAll_ShouldDelegateToRepository() {
        when(repository.findAll()).thenReturn(Flux.just(catalog));

        StepVerifier.create(adapter.findAll())
                .expectNext(catalog)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByCodigo: Retorna el catálogo cuando el código existe en BD")
    void findByCodigo_WhenExists_ShouldReturnCatalog() {
        when(repository.findByCodigo("51111001")).thenReturn(Mono.just(catalog));

        StepVerifier.create(adapter.findByCodigo("51111001"))
                .expectNext(catalog)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByCodigo: Retorna vacío cuando el código NO existe en BD")
    void findByCodigo_WhenNotExists_ShouldReturnEmpty() {
        when(repository.findByCodigo("NOEXISTE")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCodigo("NOEXISTE"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findDistinctGrupos: Retorna grupos distintos desde el repositorio")
    void findDistinctGrupos_ShouldReturnFlux() {
        when(repository.findDistinctGrupos()).thenReturn(Flux.just("MOBILIARIO", "VEHICULOS"));

        StepVerifier.create(adapter.findDistinctGrupos())
                .expectNext("MOBILIARIO", "VEHICULOS")
                .verifyComplete();
    }

    @Test
    @DisplayName("findByGrupo: Retorna registros filtrados por grupo desde el repositorio")
    void findByGrupo_ShouldReturnFlux() {
        when(repository.findByGrupo("MOBILIARIO")).thenReturn(Flux.just(catalog));

        StepVerifier.create(adapter.findByGrupo("MOBILIARIO"))
                .expectNext(catalog)
                .verifyComplete();
    }
}

package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.patrimonio_service.application.dto.SbnCatalogResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.SbnCatalogUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SbnCatalogRestController.class)
@DisplayName("SbnCatalogRestController - Controlador REST del Catálogo SBN")
class SbnCatalogRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SbnCatalogUseCase sbnCatalogUseCase;


    private SbnCatalogResponse createResponse() {
        SbnCatalogResponse r = new SbnCatalogResponse();
        r.setCodigo("51111001");
        r.setDescripcion("Escritorio de madera");
        r.setGrupo("MOBILIARIO");
        r.setClase("Muebles de Oficina");
        r.setTasaDepreciacionAnual(new BigDecimal("10"));
        r.setVidaUtilMeses(120);
        r.setEsDepreciable(true);
        r.setRequiereSerieMarcaModelo(false);
        return r;
    }

    @Test
    @DisplayName("[GET /api/v1/assets/sbn-catalog] Retorna todo el catálogo SBN")
    void getAll_ShouldReturnStatusOk() {
        when(sbnCatalogUseCase.getAll()).thenReturn(Flux.just(createResponse()));

        webTestClient.get()
                .uri("/api/v1/assets/sbn-catalog")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].codigo").isEqualTo("51111001");
    }

    @Test
    @DisplayName("[GET /api/v1/assets/sbn-catalog/grupos] Retorna lista de grupos SBN")
    void getGrupos_ShouldReturnStatusOk() {
        when(sbnCatalogUseCase.getGrupos()).thenReturn(Flux.just("MOBILIARIO", "VEHICULOS"));

        webTestClient.get()
                .uri("/api/v1/assets/sbn-catalog/grupos")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("[GET /api/v1/assets/sbn-catalog/grupo/{grupo}] Retorna registros filtrados por grupo")
    void getByGrupo_ShouldReturnStatusOk() {
        when(sbnCatalogUseCase.getByGrupo("MOBILIARIO")).thenReturn(Flux.just(createResponse()));

        webTestClient.get()
                .uri("/api/v1/assets/sbn-catalog/grupo/MOBILIARIO")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("[GET /api/v1/assets/sbn-catalog/{codigo}] Retorna registro por código cuando existe")
    void getByCodigo_ShouldReturnStatusOk() {
        when(sbnCatalogUseCase.getByCodigo("51111001")).thenReturn(Mono.just(createResponse()));

        webTestClient.get()
                .uri("/api/v1/assets/sbn-catalog/51111001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("51111001");
    }

    @Test
    @DisplayName("[GET /api/v1/assets/sbn-catalog/{codigo}] Retorna 200 con body vacío cuando el código NO existe")
    void getByCodigo_WhenNotExists_ShouldReturnEmpty() {
        when(sbnCatalogUseCase.getByCodigo("NOEXISTE")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/assets/sbn-catalog/NOEXISTE")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}

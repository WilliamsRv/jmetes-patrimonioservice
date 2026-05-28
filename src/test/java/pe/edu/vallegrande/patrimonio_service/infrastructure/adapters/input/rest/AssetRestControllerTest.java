package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AssetRestController.class)
@DisplayName("AssetRestController - Controlador REST de Bienes")
class AssetRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AssetUseCase assetUseCase;


    @Test
    void getAll_ShouldReturnStatusOk() {
        when(assetUseCase.getAll()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/assets")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getById_WhenExists_ShouldReturnAsset() {
        UUID id = UUID.randomUUID();
        AssetResponse response = new AssetResponse();
        response.setId(id);
        
        when(assetUseCase.getById(id)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/assets/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void create_WithValidBody_ShouldReturnCreated() {
        AssetRequest request = new AssetRequest();
        AssetResponse response = new AssetResponse();
        when(assetUseCase.create(any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void update_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        AssetRequest request = new AssetRequest();
        AssetResponse response = new AssetResponse();
        when(assetUseCase.update(eq(id), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/assets/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_ShouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        when(assetUseCase.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/assets/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void findByStatus_ShouldReturnFlux() {
        when(assetUseCase.findByStatus("AC")).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/assets/status/AC")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void findByAssetCode_ShouldReturnMono() {
        when(assetUseCase.findByAssetCode("CODE")).thenReturn(Mono.just(new AssetResponse()));

        webTestClient.get()
                .uri("/api/v1/assets/code/CODE")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void nextSeq_ShouldReturnResponse() {
        when(assetUseCase.findNextSequence("SBN")).thenReturn(Mono.just(new NextSeqResponse()));

        webTestClient.get()
                .uri("/api/v1/assets/next-seq/SBN")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void changeStatus_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado("BA");
        request.setObservaciones("Baja del activo");
        
        AssetResponse response = new AssetResponse();
        response.setId(id);
        response.setAssetStatus("BA");
        
        when(assetUseCase.changeStatus(eq(id), any())).thenReturn(Mono.just(response));

        webTestClient.patch()
                .uri("/api/v1/assets/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getById_WhenNotFound_ShouldReturnNotFound() {
        UUID id = UUID.randomUUID();
        when(assetUseCase.getById(id)).thenReturn(Mono.error(new pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException("Not found")));

        webTestClient.get()
                .uri("/api/v1/assets/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findLastAssetCode_ShouldReturnOk() {
        when(assetUseCase.findLastAssetCodeStartingWith("PRE")).thenReturn(Mono.just("PRE-001"));

        webTestClient.get()
                .uri("/api/v1/assets/last-code/PRE")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("[GET /api/v1/assets/validate-sbn/{sbnCode}] Retorna exists=true cuando el código SBN ya tiene bienes registrados")
    void validateSbnCode_WhenCodeExists_ShouldReturnExists() {
        String sbnCode = "51111001";
        AssetResponse asset = new AssetResponse();
        asset.setAssetCode("51111001-001");
        asset.setDescription("Escritorio de madera");

        when(assetUseCase.findLastAssetCodeStartingWith(sbnCode)).thenReturn(Mono.just("51111001-001"));
        when(assetUseCase.findByAssetCode("51111001-001")).thenReturn(Mono.just(asset));

        webTestClient.get()
                .uri("/api/v1/assets/validate-sbn/{sbnCode}", sbnCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SBNValidationResponse.class)
                .value(res -> {
                    org.junit.jupiter.api.Assertions.assertTrue(res.isExists());
                    org.junit.jupiter.api.Assertions.assertEquals("51111001-001", res.getAssetCode());
                    org.junit.jupiter.api.Assertions.assertEquals("Escritorio de madera", res.getDescription());
                });
    }

    @Test
    @DisplayName("[GET /api/v1/assets/validate-sbn/{sbnCode}] Retorna exists=false cuando el código SBN NO tiene bienes registrados")
    void validateSbnCode_WhenLastCodeNull_ShouldReturnNotExists() {
        String sbnCode = "99999999";
        when(assetUseCase.findLastAssetCodeStartingWith(sbnCode)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/assets/validate-sbn/{sbnCode}", sbnCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SBNValidationResponse.class)
                .value(res -> org.junit.jupiter.api.Assertions.assertFalse(res.isExists()));
    }

    @Test
    @DisplayName("[GET /api/v1/assets/validate-sbn/{sbnCode}] Retorna exists=false cuando el último código SBN no corresponde a un bien existente")
    void validateSbnCode_WhenAssetNotFound_ShouldReturnNotExists() {
        String sbnCode = "51111001";
        when(assetUseCase.findLastAssetCodeStartingWith(sbnCode)).thenReturn(Mono.just("51111001-001"));
        when(assetUseCase.findByAssetCode("51111001-001")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/assets/validate-sbn/{sbnCode}", sbnCode)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SBNValidationResponse.class)
                .value(res -> org.junit.jupiter.api.Assertions.assertFalse(res.isExists()));
    }
}

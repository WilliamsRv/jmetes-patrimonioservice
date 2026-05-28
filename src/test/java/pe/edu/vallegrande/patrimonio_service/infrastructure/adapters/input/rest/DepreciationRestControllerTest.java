package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.DepreciationUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = DepreciationRestController.class)
class DepreciationRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DepreciationUseCase depreciationUseCase;


    @Test
    void getAll_ShouldReturnStatusOk() {
        when(depreciationUseCase.getAll()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/assets-depreciations")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void create_WithValidBody_ShouldReturnCreated() {
        DepreciationRequest request = new DepreciationRequest();
        DepreciationResponse response = new DepreciationResponse();
        response.setId(UUID.randomUUID());

        when(depreciationUseCase.create(any(DepreciationRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/assets-depreciations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void delete_ShouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        when(depreciationUseCase.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/assets-depreciations/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getByAssetId_ShouldReturnFlux() {
        UUID assetId = UUID.randomUUID();
        when(depreciationUseCase.getByAssetId(assetId)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/assets-depreciations/asset/{assetId}", assetId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByFiscalYear_ShouldReturnFlux() {
        when(depreciationUseCase.getByFiscalYear(2026)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/assets-depreciations/year/2026")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void approve_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        DepreciationResponse response = new DepreciationResponse();
        response.setId(id);
        
        when(depreciationUseCase.approve(id, approverId)).thenReturn(Mono.just(response));

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/assets-depreciations/{id}/approve")
                        .queryParam("approvedBy", approverId.toString())
                        .build(id))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void generateAutomaticDepreciations_ShouldReturnOk() {
        UUID assetId = UUID.randomUUID();
        when(depreciationUseCase.generateAutomaticDepreciations(any(), any(), any(), eq(12), any()))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/assets-depreciations/auto/{assetId}")
                        .queryParam("initialValue", "1000")
                        .queryParam("residualValue", "100")
                        .queryParam("usefulLifeMonths", "12")
                        .queryParam("acquisitionDate", "2024-01-01T00:00:00")
                        .build(assetId))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByAssetAndPeriod_ShouldReturnOk() {
        UUID assetId = UUID.randomUUID();
        DepreciationResponse response = new DepreciationResponse();
        
        when(depreciationUseCase.getByAssetAndPeriod(assetId, 2026, 4)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/assets-depreciations/asset/{assetId}/year/2026/month/4", assetId)
                .exchange()
                .expectStatus().isOk();
    }
}

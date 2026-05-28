package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalDetailRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalDetailResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.ExecuteRemovalRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.TechnicalOpinionRequest;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalDetailUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AssetDisposalDetailRestController.class)
class AssetDisposalDetailRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AssetDisposalDetailUseCase assetDisposalDetailUseCase;


    @Test
    void getAll_ShouldReturnStatusOk() {
        when(assetDisposalDetailUseCase.getByDisposalId(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposal-details/disposal/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void create_WithValidBody_ShouldReturnOk() {
        AssetDisposalDetailRequest request = new AssetDisposalDetailRequest();
        AssetDisposalDetailResponse response = new AssetDisposalDetailResponse();
        response.setId(UUID.randomUUID());

        when(assetDisposalDetailUseCase.create(any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/asset-disposal-details")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void create_WhenError_ShouldHandleError() {
        AssetDisposalDetailRequest request = new AssetDisposalDetailRequest();
        when(assetDisposalDetailUseCase.create(any())).thenReturn(Mono.error(new RuntimeException("Error creating")));

        webTestClient.post()
                .uri("/api/v1/asset-disposal-details")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getById_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        AssetDisposalDetailResponse response = new AssetDisposalDetailResponse();
        response.setId(id);

        when(assetDisposalDetailUseCase.getById(id)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/asset-disposal-details/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByDisposalId_ShouldReturnFlux() {
        UUID disposalId = UUID.randomUUID();
        when(assetDisposalDetailUseCase.getByDisposalId(disposalId)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposal-details/disposal/{disposalId}", disposalId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByAssetId_ShouldReturnFlux() {
        UUID assetId = UUID.randomUUID();
        when(assetDisposalDetailUseCase.getByAssetId(assetId)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposal-details/asset/{assetId}", assetId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addTechnicalOpinion_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        TechnicalOpinionRequest request = new TechnicalOpinionRequest();
        request.setTechnicalOpinion("Test opinion");
        request.setRecommendation("KEEP");

        AssetDisposalDetailResponse response = new AssetDisposalDetailResponse();
        response.setId(id);

        when(assetDisposalDetailUseCase.addTechnicalOpinion(any(), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/asset-disposal-details/{id}/technical-opinion", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void executeRemoval_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        ExecuteRemovalRequest request = new ExecuteRemovalRequest();
        request.setRemovalResponsibleId(UUID.randomUUID());
        request.setFinalDestination("RECYCLING");

        AssetDisposalDetailResponse response = new AssetDisposalDetailResponse();
        response.setId(id);

        when(assetDisposalDetailUseCase.executeRemoval(any(), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/asset-disposal-details/{id}/execute-removal", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        when(assetDisposalDetailUseCase.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/asset-disposal-details/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getActiveAssetIds_ShouldReturnFlux() {
        when(assetDisposalDetailUseCase.findActiveAssetIds()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposal-details/active-asset-ids")
                .exchange()
                .expectStatus().isOk();
    }
}

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

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AssetDisposalRestController.class)
class AssetDisposalRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AssetDisposalUseCase assetDisposalUseCase;


    @Test
    void getAll_ShouldReturnStatusOk() {
        when(assetDisposalUseCase.getAll()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposals")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void create_WithValidBody_ShouldReturnOk() {
        AssetDisposalRequest request = new AssetDisposalRequest();
        AssetDisposalResponse response = new AssetDisposalResponse();
        when(assetDisposalUseCase.create(any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/asset-disposals")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByStatus_ShouldReturnFlux() {
        when(assetDisposalUseCase.getByStatus("INITIATED")).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposals/status/INITIATED")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cancel_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        when(assetDisposalUseCase.cancel(eq(id), any())).thenReturn(Mono.just(new AssetDisposalResponse()));

        webTestClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/asset-disposals/{id}/cancel")
                        .queryParam("cancelledBy", UUID.randomUUID().toString())
                        .build(id))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_ShouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        when(assetDisposalUseCase.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/asset-disposals/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getById_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        AssetDisposalResponse response = new AssetDisposalResponse();
        response.setId(id);
        when(assetDisposalUseCase.getById(id)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/asset-disposals/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByFileNumber_ShouldReturnOk() {
        AssetDisposalResponse response = new AssetDisposalResponse();
        response.setFileNumber("EXP-2024-001");
        when(assetDisposalUseCase.getByFileNumber("EXP-2024-001")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/asset-disposals/file-number/EXP-2024-001")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getByRequestedBy_ShouldReturnFlux() {
        UUID userId = UUID.randomUUID();
        when(assetDisposalUseCase.getByRequestedBy(userId)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/asset-disposals/requested-by/{userId}", userId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void assignCommittee_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        pe.edu.vallegrande.patrimonio_service.application.dto.AssignCommitteeRequest request = new pe.edu.vallegrande.patrimonio_service.application.dto.AssignCommitteeRequest();
        request.setAssignedBy(UUID.randomUUID());
        
        AssetDisposalResponse response = new AssetDisposalResponse();
        when(assetDisposalUseCase.assignCommittee(eq(id), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/asset-disposals/{id}/assign-committee", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void resolve_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        pe.edu.vallegrande.patrimonio_service.application.dto.ResolveDisposalRequest request = new pe.edu.vallegrande.patrimonio_service.application.dto.ResolveDisposalRequest();
        request.setApproved(true);
        request.setResolutionNumber("RES-001");
        
        AssetDisposalResponse response = new AssetDisposalResponse();
        when(assetDisposalUseCase.resolve(eq(id), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/asset-disposals/{id}/resolve", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void completeAssetDisposal_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        AssetDisposalResponse response = new AssetDisposalResponse();
        when(assetDisposalUseCase.completeAssetDisposal(id)).thenReturn(Mono.just(response));

        webTestClient.patch()
                .uri("/api/v1/asset-disposals/{id}/complete", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void restore_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        AssetDisposalResponse response = new AssetDisposalResponse();
        when(assetDisposalUseCase.restore(id)).thenReturn(Mono.just(response));

        webTestClient.patch()
                .uri("/api/v1/asset-disposals/{id}/restore", id)
                .exchange()
                .expectStatus().isOk();
    }
}

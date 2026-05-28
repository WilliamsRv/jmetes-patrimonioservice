package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalDetailWithAssetName;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetDisposalDetailNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AssetDisposalDetailServiceTest {

    @Mock
    private AssetDisposalDetailPersistencePort persistencePort;

    @Mock
    private AssetDisposalDetailRepository repository;

    @InjectMocks
    private AssetDisposalDetailService detailService;

    private UUID detailId;
    private UUID disposalId;
    private UUID assetId;
    private AssetDisposalDetail detail;

    @BeforeEach
    void setUp() {
        detailId = UUID.randomUUID();
        disposalId = UUID.randomUUID();
        assetId = UUID.randomUUID();

        detail = new AssetDisposalDetail();
        detail.setId(detailId);
        detail.setDisposalId(disposalId);
        detail.setAssetId(assetId);
        detail.setRecommendation("DISPOSE");
    }

    @Test
    void getById_WhenExists_ShouldReturnResponse() {
        when(persistencePort.findById(detailId)).thenReturn(Mono.just(detail));

        StepVerifier.create(detailService.getById(detailId))
                .expectNextMatches(res -> res.getId().equals(detailId))
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        when(persistencePort.findById(detailId)).thenReturn(Mono.empty());

        StepVerifier.create(detailService.getById(detailId))
                .expectError(AssetDisposalDetailNotFoundException.class)
                .verify();
    }

    @Test
    void getByDisposalId_ShouldReturnFlux() {
        AssetDisposalDetailWithAssetName dto = new AssetDisposalDetailWithAssetName();
        dto.setId(detailId);
        dto.setAssetId(assetId);
        dto.setAssetCode("AST-001");
        dto.setDescription("Test Asset");

        when(repository.findByDisposalIdWithAssetName(disposalId)).thenReturn(Flux.just(dto));

        StepVerifier.create(detailService.getByDisposalId(disposalId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByAssetId_ShouldReturnFlux() {
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(detail));

        StepVerifier.create(detailService.getByAssetId(assetId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void create_WhenAssetNotInActiveDisposal_ShouldCreate() {
        AssetDisposalDetailRequest request = new AssetDisposalDetailRequest();
        request.setAssetId(assetId);
        request.setDisposalId(disposalId);
        request.setConservationStatus("GOOD");
        request.setObservations("Test observations");

        when(persistencePort.existsByAssetIdInActiveDisposal(assetId)).thenReturn(Mono.just(false));
        when(persistencePort.save(any(AssetDisposalDetail.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(detailService.create(request))
                .expectNextMatches(res -> res.getAssetId().equals(assetId))
                .verifyComplete();
    }

    @Test
    void create_WhenAssetAlreadyInActiveDisposal_ShouldThrowException() {
        AssetDisposalDetailRequest request = new AssetDisposalDetailRequest();
        request.setAssetId(assetId);
        request.setDisposalId(disposalId);

        when(persistencePort.existsByAssetIdInActiveDisposal(assetId)).thenReturn(Mono.just(true));

        StepVerifier.create(detailService.create(request))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void addTechnicalOpinion_WhenExists_ShouldUpdate() {
        TechnicalOpinionRequest request = new TechnicalOpinionRequest();
        request.setTechnicalOpinion("Opinion test");
        request.setRecommendation("KEEP");
        request.setObservations("Some observations");

        when(persistencePort.findById(detailId)).thenReturn(Mono.just(detail));
        when(repository.updateTechnicalOpinion(eq(detailId), eq("Opinion test"), eq("KEEP"), eq("Some observations"), any()))
                .thenReturn(Mono.empty());
        when(persistencePort.findById(detailId)).thenReturn(Mono.just(detail));

        StepVerifier.create(detailService.addTechnicalOpinion(detailId, request))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addTechnicalOpinion_WhenNotExists_ShouldThrowException() {
        TechnicalOpinionRequest request = new TechnicalOpinionRequest();

        when(persistencePort.findById(detailId)).thenReturn(Mono.empty());

        StepVerifier.create(detailService.addTechnicalOpinion(detailId, request))
                .expectError(AssetDisposalDetailNotFoundException.class)
                .verify();
    }

    @Test
    void executeRemoval_WhenExists_ShouldUpdate() {
        ExecuteRemovalRequest request = new ExecuteRemovalRequest();
        request.setRemovalResponsibleId(UUID.randomUUID());
        request.setFinalDestination("Recycling");

        when(persistencePort.findById(detailId)).thenReturn(Mono.just(detail));
        when(persistencePort.save(any(AssetDisposalDetail.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(detailService.executeRemoval(detailId, request))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void executeRemoval_WhenNotExists_ShouldThrowException() {
        ExecuteRemovalRequest request = new ExecuteRemovalRequest();

        when(persistencePort.findById(detailId)).thenReturn(Mono.empty());

        StepVerifier.create(detailService.executeRemoval(detailId, request))
                .expectError(AssetDisposalDetailNotFoundException.class)
                .verify();
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        when(persistencePort.findById(detailId)).thenReturn(Mono.just(detail));
        when(persistencePort.deleteById(detailId)).thenReturn(Mono.empty());

        StepVerifier.create(detailService.delete(detailId))
                .verifyComplete();

        verify(persistencePort).deleteById(detailId);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        when(persistencePort.findById(detailId)).thenReturn(Mono.empty());

        StepVerifier.create(detailService.delete(detailId))
                .expectError(AssetDisposalDetailNotFoundException.class)
                .verify();
    }

    @Test
    void findActiveAssetIds_ShouldReturnFlux() {
        UUID activeAssetId = UUID.randomUUID();
        when(persistencePort.findActiveAssetIds()).thenReturn(Flux.just(activeAssetId));

        StepVerifier.create(detailService.findActiveAssetIds())
                .expectNext(activeAssetId)
                .verifyComplete();
    }
}

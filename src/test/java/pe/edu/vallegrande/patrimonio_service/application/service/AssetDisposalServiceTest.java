package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetStatusPort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetDisposalNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.exception.InvalidDisposalStateException;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AssetDisposalServiceTest {

    @Mock
    private AssetDisposalPersistencePort persistencePort;

    @Mock
    private AssetStatusPort assetStatusPort;

    @Mock
    private AssetDisposalDetailPort assetDisposalDetailPort;

    @InjectMocks
    private AssetDisposalService assetDisposalService;

    private UUID disposalId;
    private AssetDisposal disposal;

    @BeforeEach
    void setUp() {
        disposalId = UUID.randomUUID();
        disposal = new AssetDisposal();
        disposal.setId(disposalId);
        disposal.setFileNumber("BAJA-2026-0001");
        disposal.setFileStatus("INITIATED");
    }

    @Test
    void getById_WhenExists_ShouldReturnResponse() {
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        StepVerifier.create(assetDisposalService.getById(disposalId))
                .expectNextMatches(res -> res.getId().equals(disposalId))
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());
        StepVerifier.create(assetDisposalService.getById(disposalId))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void getAll_ShouldReturnFlux() {
        when(persistencePort.findAll()).thenReturn(Flux.just(disposal));
        StepVerifier.create(assetDisposalService.getAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByStatus_ShouldReturnFlux() {
        when(persistencePort.findByFileStatus("PENDING")).thenReturn(Flux.just(disposal));
        StepVerifier.create(assetDisposalService.getByStatus("PENDING"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByFileNumber_ShouldReturnMono() {
        when(persistencePort.findByFileNumber("BAJA-001")).thenReturn(Mono.just(disposal));
        StepVerifier.create(assetDisposalService.getByFileNumber("BAJA-001"))
                .expectNextMatches(res -> res.getFileNumber().equals(disposal.getFileNumber()))
                .verifyComplete();
    }

    @Test
    void getByRequestedBy_ShouldReturnFlux() {
        UUID userId = UUID.randomUUID();
        when(persistencePort.findByRequestedBy(userId)).thenReturn(Flux.just(disposal));
        StepVerifier.create(assetDisposalService.getByRequestedBy(userId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void assignCommittee_WhenStatusInitiated_ShouldUpdateStatus() {
        disposal.setFileStatus("INITIATED");
        AssignCommitteeRequest req = new AssignCommitteeRequest();
        req.setAssignedBy(UUID.randomUUID());

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.assignCommittee(disposalId, req))
                .expectNextMatches(res -> res.getFileStatus().equals("UNDER_EVALUATION"))
                .verifyComplete();
    }

    @Test
    void assignCommittee_WhenStatusInvalid_ShouldThrowException() {
        disposal.setFileStatus("APPROVED");
        AssignCommitteeRequest req = new AssignCommitteeRequest();
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));

        StepVerifier.create(assetDisposalService.assignCommittee(disposalId, req))
                .expectError(InvalidDisposalStateException.class)
                .verify();
    }

    @Test
    void resolve_WhenApproved_ShouldUpdateToApprovedAndProcessAssets() {
        disposal.setFileStatus("UNDER_EVALUATION");
        ResolveDisposalRequest req = new ResolveDisposalRequest();
        req.setApproved(true);
        req.setApprovedById(UUID.randomUUID());

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(disposalId)).thenReturn(Flux.empty());
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.resolve(disposalId, req))
                .expectNextMatches(res -> res.getFileStatus().equals("APPROVED"))
                .verifyComplete();
    }

    @Test
    void resolve_WhenRejected_ShouldUpdateToRejected() {
        disposal.setFileStatus("UNDER_EVALUATION");
        ResolveDisposalRequest req = new ResolveDisposalRequest();
        req.setApproved(false);

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.resolve(disposalId, req))
                .expectNextMatches(res -> res.getFileStatus().equals("REJECTED"))
                .verifyComplete();
    }

    @Test
    void cancel_WhenNotExecuted_ShouldUpdateToCancelled() {
        disposal.setFileStatus("INITIATED");
        UUID cancelledBy = UUID.randomUUID();

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.cancel(disposalId, cancelledBy))
                .expectNextMatches(res -> res.getFileStatus().equals("CANCELLED"))
                .verifyComplete();
    }

    @Test
    void cancel_WhenExecuted_ShouldThrowException() {
        disposal.setFileStatus("EXECUTED");
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));

        StepVerifier.create(assetDisposalService.cancel(disposalId, UUID.randomUUID()))
                .expectError(InvalidDisposalStateException.class)
                .verify();
    }

    @Test
    void completeAssetDisposal_WhenApproved_ShouldUpdateToExecuted() {
        disposal.setFileStatus("APPROVED");
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(disposalId)).thenReturn(Flux.empty());
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.completeAssetDisposal(disposalId))
                .expectNextMatches(res -> res.getFileStatus().equals("EXECUTED"))
                .verifyComplete();
    }

    @Test
    void completeAssetDisposal_WhenNotApproved_ShouldThrowException() {
        disposal.setFileStatus("PENDING");
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));

        StepVerifier.create(assetDisposalService.completeAssetDisposal(disposalId))
                .expectError(InvalidDisposalStateException.class)
                .verify();
    }

    @Test
    void restore_WhenExecuted_ShouldUpdateToRestored() {
        disposal.setFileStatus("EXECUTED");
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(disposalId)).thenReturn(Flux.empty());
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.restore(disposalId))
                .expectNextMatches(res -> res.getFileStatus().equals("RESTORED"))
                .verifyComplete();
    }

    @Test
    void restore_WhenExecutedWithDetails_ShouldUpdateAssetStatus() {
        disposal.setFileStatus("EXECUTED");
        UUID assetId = UUID.randomUUID();
        AssetDisposalDetail detail = new AssetDisposalDetail();
        detail.setAssetId(assetId);

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(disposalId)).thenReturn(Flux.just(detail));
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(assetStatusPort.updateAssetStatusToAvailable(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.restore(disposalId))
                .expectNextMatches(res -> res.getFileStatus().equals("RESTORED"))
                .verifyComplete();

        verify(assetStatusPort).updateAssetStatusToAvailable(eq(assetId), any());
    }

    @Test
    void delete_ShouldCallPersistence() {
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(persistencePort.deleteById(disposalId)).thenReturn(Mono.empty());
        StepVerifier.create(assetDisposalService.delete(disposalId))
                .verifyComplete();
        verify(persistencePort).deleteById(disposalId);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowException() {
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());
        StepVerifier.create(assetDisposalService.delete(disposalId))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void create_WhenDuplicateKeyException_ShouldRetry() {
        AssetDisposalRequest request = new AssetDisposalRequest();
        request.setReasonDescription("Test reason");

        when(persistencePort.existsByFileNumber(anyString())).thenReturn(Mono.just(false));
        when(persistencePort.save(any(AssetDisposal.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate"))
                .thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(any())).thenReturn(Flux.empty());

        StepVerifier.create(assetDisposalService.create(request))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void resolve_WhenApprovedTrue_ShouldCallUpdateAssetsStatus() {
        disposal.setFileStatus("UNDER_EVALUATION");
        ResolveDisposalRequest req = new ResolveDisposalRequest();
        req.setApproved(true);
        req.setApprovedById(UUID.randomUUID());

        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));
        when(assetDisposalDetailPort.findByDisposalId(disposalId)).thenReturn(Flux.empty());
        when(persistencePort.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(assetDisposalService.resolve(disposalId, req))
                .expectNextMatches(res -> res.getFileStatus().equals("APPROVED"))
                .verifyComplete();

        verify(assetDisposalDetailPort).findByDisposalId(disposalId);
    }

    @Test
    void restore_WhenNotExecuted_ShouldThrowException() {
        disposal.setFileStatus("APPROVED");
        when(persistencePort.findById(disposalId)).thenReturn(Mono.just(disposal));

        StepVerifier.create(assetDisposalService.restore(disposalId))
                .expectError(InvalidDisposalStateException.class)
                .verify();
    }

    @Test
    void getByFileNumber_WhenNotFound_ShouldThrowException() {
        String fileNumber = "BAJA-999";
        when(persistencePort.findByFileNumber(fileNumber)).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.getByFileNumber(fileNumber))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void assignCommittee_WhenDisposalNotFound_ShouldThrowException() {
        AssignCommitteeRequest req = new AssignCommitteeRequest();
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.assignCommittee(disposalId, req))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void cancel_WhenDisposalNotFound_ShouldThrowException() {
        UUID cancelledBy = UUID.randomUUID();
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.cancel(disposalId, cancelledBy))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void completeAssetDisposal_WhenDisposalNotFound_ShouldThrowException() {
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.completeAssetDisposal(disposalId))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }

    @Test
    void resolve_WhenDisposalNotFound_ShouldThrowException() {
        ResolveDisposalRequest req = new ResolveDisposalRequest();
        when(persistencePort.findById(disposalId)).thenReturn(Mono.empty());

        StepVerifier.create(assetDisposalService.resolve(disposalId, req))
                .expectError(AssetDisposalNotFoundException.class)
                .verify();
    }
}

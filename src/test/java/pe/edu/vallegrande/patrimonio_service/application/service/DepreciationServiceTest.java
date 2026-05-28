package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.DepreciationPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.DepreciationNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepreciationService - Servicio de Depreciación")
class DepreciationServiceTest {

    @Mock
    private DepreciationPersistencePort persistencePort;

    @Mock
    private AssetPersistencePort assetPersistencePort;

    @InjectMocks
    private DepreciationService depreciationService;

    private UUID depreciationId;
    private Depreciation depreciation;

    @BeforeEach
    void setUp() {
        depreciationId = UUID.randomUUID();
        depreciation = new Depreciation();
        depreciation.setId(depreciationId);
        depreciation.setFiscalYear(2026);
        depreciation.setCalculationStatus("CALCULATED");
    }

    @Test
    void getById_WhenExists_ShouldReturnResponse() {
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.just(depreciation));

        StepVerifier.create(depreciationService.getById(depreciationId))
                .expectNextMatches(res -> res.getId().equals(depreciationId))
                .verifyComplete();
    }

    @Test
    void getById_WhenNotExists_ShouldThrowException() {
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.getById(depreciationId))
                .expectError(DepreciationNotFoundException.class)
                .verify();
    }

    @Test
    void getAll_ShouldReturnFlux() {
        when(persistencePort.findAll()).thenReturn(Flux.just(depreciation));

        StepVerifier.create(depreciationService.getAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void create_WhenValidRequest_ShouldCalculateValuesAndSave() {
        pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest req = new pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest();
        req.setInitialValue(new BigDecimal("1200.00"));
        req.setUsefulLifeYears(1);
        req.setResidualValue(BigDecimal.ZERO);

        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depreciationService.create(req))
                .expectNextMatches(res -> res.getAnnualDepreciation().compareTo(new BigDecimal("1200.00")) == 0
                        && res.getMonthlyDepreciation().compareTo(new BigDecimal("100.00")) == 0)
                .verifyComplete();
    }

    @Test
    void create_WithResidualValue_ShouldCalculateCorrectly() {
        DepreciationRequest req = new DepreciationRequest();
        req.setInitialValue(new BigDecimal("5000.00"));
        req.setResidualValue(new BigDecimal("500.00"));
        req.setUsefulLifeYears(5);

        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depreciationService.create(req))
                .assertNext(res -> {
                    assertEquals(0, new BigDecimal("900.00").compareTo(res.getAnnualDepreciation()));
                    assertEquals(0, new BigDecimal("75.00").compareTo(res.getMonthlyDepreciation()));
                })
                .verifyComplete();
    }

    @Test
    void delete_WhenExists_ShouldDeleteSuccessfully() {
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.just(depreciation));
        when(persistencePort.deleteById(depreciationId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.delete(depreciationId))
                .verifyComplete();

        verify(persistencePort).deleteById(depreciationId);
    }

    @Test
    void generateAutomaticDepreciations_WithValidValues_ShouldReturnFlux() {
        UUID assetId = UUID.randomUUID();
        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Flux<pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse> result =
                depreciationService.generateAutomaticDepreciations(
                        assetId,
                        new BigDecimal("1200"),
                        BigDecimal.ZERO,
                        12,
                        java.time.LocalDateTime.now().minusMonths(6)
                );

        StepVerifier.create(result)
                .expectNextCount(7) // Today month + 6 months ago = 7
                .verifyComplete();
    }

    @Test
    void getByAssetId_ShouldReturnFlux() {
        UUID assetId = UUID.randomUUID();
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(depreciation));
        StepVerifier.create(depreciationService.getByAssetId(assetId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByFiscalYear_ShouldReturnFlux() {
        when(persistencePort.findByFiscalYear(2026)).thenReturn(Flux.just(depreciation));
        StepVerifier.create(depreciationService.getByFiscalYear(2026))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getByAssetAndPeriod_ShouldReturnMono() {
        UUID assetId = UUID.randomUUID();
        when(persistencePort.findByAssetAndPeriod(assetId, 2026, 4)).thenReturn(Mono.just(depreciation));
        StepVerifier.create(depreciationService.getByAssetAndPeriod(assetId, 2026, 4))
                .expectNextMatches(res -> res.getId().equals(depreciationId))
                .verifyComplete();
    }

    @Test
    void approve_WhenExists_ShouldUpdateStatus() {
        UUID approverId = UUID.randomUUID();
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.just(depreciation));
        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depreciationService.approve(depreciationId, approverId))
                .expectNextMatches(res -> res.getCalculationStatus().equals("APPROVED"))
                .verifyComplete();
    }

    @Test
    void approve_WhenNotFound_ShouldThrowException() {
        UUID approverId = UUID.randomUUID();
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.approve(depreciationId, approverId))
                .expectError(DepreciationNotFoundException.class)
                .verify();
    }

    @Test
    void delete_WhenNotFound_ShouldThrowException() {
        when(persistencePort.findById(depreciationId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.delete(depreciationId))
                .expectError(DepreciationNotFoundException.class)
                .verify();
    }

    @Test
    void createInitialDepreciation_WhenAcquisitionValueInvalid_ShouldThrowException() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAcquisitionValue(null);
        asset.setUsefulLife(12);

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void createInitialDepreciation_WhenAcquisitionValueZeroOrNegative_ShouldThrowException() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAcquisitionValue(BigDecimal.ZERO);
        asset.setUsefulLife(12);

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void createInitialDepreciation_WhenUsefulLifeInvalid_ShouldThrowException() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAcquisitionValue(new BigDecimal("1000.00"));
        asset.setUsefulLife(null);

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void createInitialDepreciation_WhenUsefulLifeZeroOrNegative_ShouldThrowException() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAcquisitionValue(new BigDecimal("1000.00"));
        asset.setUsefulLife(0);

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void create_WhenInitialValueZeroOrUsefulLifeInvalid_ShouldSetZeroDepreciation() {
        pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest req = new pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest();
        req.setInitialValue(BigDecimal.ZERO);
        req.setUsefulLifeYears(0);
        req.setResidualValue(BigDecimal.ZERO);

        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(depreciationService.create(req))
                .expectNextMatches(res -> res.getAnnualDepreciation().compareTo(BigDecimal.ZERO) == 0
                        && res.getMonthlyDepreciation().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void generateAutomaticDepreciations_WhenInitialValueLessThanResidual_ShouldReturnEmpty() {
        UUID assetId = UUID.randomUUID();

        Flux<pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse> result =
                depreciationService.generateAutomaticDepreciations(
                        assetId,
                        new BigDecimal("100"),
                        new BigDecimal("200"),
                        12,
                        java.time.LocalDateTime.now()
                );

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void generateAutomaticDepreciations_WhenUsefulLifeMonthsZeroOrNegative_ShouldReturnEmpty() {
        UUID assetId = UUID.randomUUID();

        Flux<pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse> result =
                depreciationService.generateAutomaticDepreciations(
                        assetId,
                        new BigDecimal("1200"),
                        BigDecimal.ZERO,
                        0,
                        java.time.LocalDateTime.now()
                );

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getByAssetAndPeriod_WhenNotFound_ShouldThrowException() {
        UUID assetId = UUID.randomUUID();
        when(persistencePort.findByAssetAndPeriod(assetId, 2026, 4)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.getByAssetAndPeriod(assetId, 2026, 4))
                .expectError(DepreciationNotFoundException.class)
                .verify();
    }

    @Test
    void deleteByAssetAndPeriod_ShouldDeleteSuccessfully() {
        UUID assetId = UUID.randomUUID();
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(depreciation));
        when(persistencePort.deleteById(depreciationId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.deleteByAssetAndPeriod(assetId, 2026, 4))
                .verifyComplete();
    }

    @Test
    void createInitialDepreciation_WhenNoRecordsGenerated_ShouldThrowException() {
        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAcquisitionValue(new BigDecimal("1000.00"));
        asset.setUsefulLife(1);
        asset.setAcquisitionDate(java.time.LocalDate.now().plusMonths(2));

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    @DisplayName("createInitialDepreciation: Crea depreciación y actualiza el asset cuando los datos son válidos")
    void createInitialDepreciation_WhenValid_ShouldCreateAndUpdateAsset() {
        UUID assetId = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setAcquisitionValue(new BigDecimal("1200.00"));
        asset.setUsefulLife(12);
        asset.setAcquisitionDate(java.time.LocalDate.now().minusMonths(1));

        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Asset savedAsset = new Asset();
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(savedAsset));
        when(assetPersistencePort.save(any(Asset.class))).thenReturn(Mono.just(new Asset()));

        StepVerifier.create(depreciationService.createInitialDepreciation(asset))
                .expectNextMatches(res -> res.getCurrentBookValue() != null)
                .verifyComplete();

        verify(persistencePort, atLeastOnce()).save(any(Depreciation.class));
        verify(assetPersistencePort).findById(assetId);
        verify(assetPersistencePort).save(any(Asset.class));
    }

    @Test
    @DisplayName("recalculateForAsset: Lanza error cuando el asset NO existe")
    void recalculateForAsset_WhenAssetNotFound_ShouldThrowException() {
        UUID assetId = UUID.randomUUID();
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(depreciationService.recalculateForAsset(assetId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("recalculateForAsset: Completado sin ajustes cuando las depreciaciones aprobadas ya están correctas")
    void recalculateForAsset_WhenHasApprovedDepsAndZeroAdjustment_ShouldComplete() {
        UUID assetId = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setAcquisitionValue(new BigDecimal("1200.00"));
        asset.setUsefulLife(12);
        asset.setAcquisitionDate(java.time.LocalDate.now().minusMonths(1));
        asset.setResidualValue(BigDecimal.ZERO);

        Depreciation approvedDep = new Depreciation();
        approvedDep.setCalculationStatus("APPROVED");
        approvedDep.setPeriodDepreciation(new BigDecimal("100.00"));
        approvedDep.setPreviousAccumulatedDepreciation(BigDecimal.ZERO);
        approvedDep.setCurrentAccumulatedDepreciation(new BigDecimal("100.00"));
        approvedDep.setCalculationDate(java.time.LocalDateTime.now().minusMonths(1).withDayOfMonth(1));

        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(approvedDep));

        StepVerifier.create(depreciationService.recalculateForAsset(assetId))
                .verifyComplete();
    }

    @Test
    @DisplayName("recalculateForAsset: Guarda registro de ajuste cuando la depreciación aprobada difiere del cálculo correcto")
    void recalculateForAsset_WhenHasApprovedDepsAndNeedsAdjustment_ShouldSaveAdjustment() {
        UUID assetId = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setAcquisitionValue(new BigDecimal("1200.00"));
        asset.setUsefulLife(12);
        asset.setAcquisitionDate(java.time.LocalDate.now().minusMonths(1));
        asset.setResidualValue(BigDecimal.ZERO);

        Depreciation approvedDep = new Depreciation();
        approvedDep.setCalculationStatus("APPROVED");
        approvedDep.setPeriodDepreciation(new BigDecimal("50.00"));
        approvedDep.setPreviousAccumulatedDepreciation(BigDecimal.ZERO);
        approvedDep.setCurrentAccumulatedDepreciation(new BigDecimal("50.00"));
        approvedDep.setCalculationDate(java.time.LocalDateTime.now().minusMonths(1).withDayOfMonth(1));

        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(approvedDep));
        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Asset savedAsset = new Asset();
        when(assetPersistencePort.save(any(Asset.class))).thenReturn(Mono.just(savedAsset));

        StepVerifier.create(depreciationService.recalculateForAsset(assetId))
                .verifyComplete();

        verify(persistencePort).save(any(Depreciation.class));
    }

    @Test
    @DisplayName("recalculateForAsset: Elimina y regenera depreciaciones cuando NO hay registros aprobados")
    void recalculateForAsset_WhenNoApprovedDeps_ShouldDeleteAndRegenerate() {
        UUID assetId = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setAcquisitionValue(new BigDecimal("1200.00"));
        asset.setUsefulLife(12);
        asset.setAcquisitionDate(java.time.LocalDate.now().minusMonths(1));

        Depreciation dep = new Depreciation();
        dep.setCalculationStatus("CALCULATED");

        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.findByAssetId(assetId)).thenReturn(Flux.just(dep));
        when(persistencePort.deleteByAssetId(assetId)).thenReturn(Mono.empty());
        when(persistencePort.save(any(Depreciation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Asset savedAsset = new Asset();
        when(assetPersistencePort.save(any(Asset.class))).thenReturn(Mono.just(savedAsset));

        StepVerifier.create(depreciationService.recalculateForAsset(assetId))
                .verifyComplete();

        verify(persistencePort).deleteByAssetId(assetId);
        verify(persistencePort, atLeastOnce()).save(any(Depreciation.class));
    }
}

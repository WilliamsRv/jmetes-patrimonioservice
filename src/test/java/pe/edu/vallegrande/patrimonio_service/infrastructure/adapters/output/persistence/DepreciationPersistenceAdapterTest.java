package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.DepreciationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepreciationPersistenceAdapter - Adaptador de persistencia de Depreciación")
class DepreciationPersistenceAdapterTest {

    @Mock
    private DepreciationRepository repository;

    @Mock
    private AssetPersistencePort assetPersistencePort;

    @InjectMocks
    private DepreciationPersistenceAdapter adapter;

    private UUID assetId;
    private Depreciation depreciation;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();
        depreciation = new Depreciation();
        depreciation.setId(UUID.randomUUID());
        depreciation.setAssetId(assetId);
    }

    @Test
    void save_WhenAssetExists_ShouldSave() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(new Asset()));
        when(repository.save(any(Depreciation.class))).thenReturn(Mono.just(depreciation));

        StepVerifier.create(adapter.save(depreciation))
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void save_WhenAssetNotFound_ShouldThrowException() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.save(depreciation))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findByAssetId_WhenAssetExists_ShouldReturnFlux() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(new Asset()));
        when(repository.findByAssetId(assetId)).thenReturn(Flux.just(depreciation));

        StepVerifier.create(adapter.findByAssetId(assetId))
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void findByAssetId_WhenAssetNotFound_ShouldThrowException() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByAssetId(assetId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findById_WhenAssetExists_ShouldReturnDepreciation() {
        when(repository.findById(depreciation.getId())).thenReturn(Mono.just(depreciation));
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(new Asset()));

        StepVerifier.create(adapter.findById(depreciation.getId()))
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void findById_WhenAssetNotFound_ShouldThrowException() {
        when(repository.findById(depreciation.getId())).thenReturn(Mono.just(depreciation));
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(depreciation.getId()))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findAll_ShouldReturnFlux() {
        when(repository.findAll()).thenReturn(Flux.just(depreciation));

        StepVerifier.create(adapter.findAll())
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void findByFiscalYear_ShouldReturnFlux() {
        when(repository.findByFiscalYear(2026)).thenReturn(Flux.just(depreciation));

        StepVerifier.create(adapter.findByFiscalYear(2026))
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void findByAssetAndPeriod_ShouldReturnMono() {
        when(repository.findByAssetIdAndFiscalYearAndCalculationMonth(assetId, 2026, 4))
                .thenReturn(Mono.just(depreciation));

        StepVerifier.create(adapter.findByAssetAndPeriod(assetId, 2026, 4))
                .expectNext(depreciation)
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldComplete() {
        when(repository.deleteById(depreciation.getId())).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(depreciation.getId()))
                .verifyComplete();
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        when(repository.existsById(depreciation.getId())).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsById(depreciation.getId()))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        when(repository.existsById(depreciation.getId())).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.existsById(depreciation.getId()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteByAssetAndPeriod_WhenAssetExists_ShouldDelete() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(new Asset()));
        when(repository.findByAssetId(assetId)).thenReturn(Flux.just(depreciation));
        depreciation.setFiscalYear(2026);
        depreciation.setCalculationMonth(4);
        when(repository.delete(depreciation)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteByAssetAndPeriod(assetId, 2026, 4))
                .verifyComplete();
    }

    @Test
    void deleteByAssetAndPeriod_WhenAssetNotFound_ShouldThrowException() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteByAssetAndPeriod(assetId, 2026, 4))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("deleteByAssetId: Elimina todas las depreciaciones de un asset cuando existe")
    void deleteByAssetId_WhenAssetExists_ShouldDelete() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.just(new Asset()));
        when(repository.deleteByAssetId(assetId)).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.deleteByAssetId(assetId))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteByAssetId: Lanza error cuando el asset NO existe")
    void deleteByAssetId_WhenAssetNotFound_ShouldThrowException() {
        when(assetPersistencePort.findById(assetId)).thenReturn(Mono.empty());
        when(repository.deleteByAssetId(assetId)).thenReturn(Mono.just(0));

        StepVerifier.create(adapter.deleteByAssetId(assetId))
                .expectError(RuntimeException.class)
                .verify();
    }
}

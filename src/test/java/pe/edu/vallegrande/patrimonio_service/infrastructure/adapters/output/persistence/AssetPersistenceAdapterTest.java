package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
class AssetPersistenceAdapterTest {

    @Mock
    private AssetRepository repository;

    @InjectMocks
    private AssetPersistenceAdapter adapter;

    private UUID municipalityId;
    private Asset asset;

    @BeforeEach
    void setUp() {
        municipalityId = UUID.randomUUID();
        asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setAssetCode("TEST-001");
    }

    @Test
    void save_ShouldSetMunicipalityIdAndSave() {
        when(repository.save(any(Asset.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(adapter.save(asset).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNextMatches(saved -> saved.getMunicipalityId().equals(municipalityId))
                .verifyComplete();
    }

    @Test
    void findById_ShouldCallRepositoryWithMunicipalityId() {
        UUID id = asset.getId();
        when(repository.findByIdAndMunicipalityId(eq(id), eq(municipalityId))).thenReturn(Mono.just(asset));

        StepVerifier.create(adapter.findById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void findAll_ShouldCallRepositoryWithMunicipalityId() {
        when(repository.findAllByMunicipalityId(municipalityId)).thenReturn(Flux.just(asset));

        StepVerifier.create(adapter.findAll().contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void deleteById_WhenExists_ShouldDelete() {
        UUID id = asset.getId();
        when(repository.findByIdAndMunicipalityId(id, municipalityId)).thenReturn(Mono.just(asset));
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .verifyComplete();
    }

    @Test
    void deleteById_WhenNotExists_ShouldThrowException() {
        UUID id = asset.getId();
        when(repository.findByIdAndMunicipalityId(id, municipalityId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findByAssetCode_ShouldCallRepository() {
        String code = "TEST-001";
        when(repository.findByAssetCodeAndMunicipalityId(code, municipalityId)).thenReturn(Mono.just(asset));

        StepVerifier.create(adapter.findByAssetCode(code).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void findLastAssetCodeStartingWith_ShouldCallRepository() {
        String prefix = "TEST";
        when(repository.findTopAssetCodeByPrefixAndMunicipalityId(prefix, municipalityId)).thenReturn(Mono.just("TEST-999"));

        StepVerifier.create(adapter.findLastAssetCodeStartingWith(prefix).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext("TEST-999")
                .verifyComplete();
    }

    @Test
    void findLastAssetCodeStartingWith_WhenNotFound_ShouldReturnNull() {
        String prefix = "TEST";
        when(repository.findTopAssetCodeByPrefixAndMunicipalityId(prefix, municipalityId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findLastAssetCodeStartingWith(prefix).contextWrite(Context.of("municipalityId", municipalityId)))
                .verifyComplete();
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        when(repository.findByIdAndMunicipalityId(asset.getId(), municipalityId)).thenReturn(Mono.just(asset));

        StepVerifier.create(adapter.existsById(asset.getId()).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        when(repository.findByIdAndMunicipalityId(asset.getId(), municipalityId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.existsById(asset.getId()).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByAssetStatus_ShouldCallRepository() {
        when(repository.findByAssetStatusAndMunicipalityId("AC", municipalityId)).thenReturn(Flux.just(asset));

        StepVerifier.create(adapter.findByAssetStatus("AC").contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void findByCurrentLocationId_ShouldCallRepository() {
        UUID locationId = UUID.randomUUID();
        when(repository.findByCurrentLocationIdAndMunicipalityId(locationId, municipalityId)).thenReturn(Flux.just(asset));

        StepVerifier.create(adapter.findByCurrentLocationId(locationId).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void findByCurrentResponsibleId_ShouldCallRepository() {
        UUID responsibleId = UUID.randomUUID();
        when(repository.findByCurrentResponsibleIdAndMunicipalityId(responsibleId, municipalityId)).thenReturn(Flux.just(asset));

        StepVerifier.create(adapter.findByCurrentResponsibleId(responsibleId).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(asset)
                .verifyComplete();
    }

    @Test
    void countByAssetStatus_ShouldCallRepository() {
        when(repository.countByAssetStatusAndMunicipalityId("AC", municipalityId)).thenReturn(Mono.just(10L));

        StepVerifier.create(adapter.countByAssetStatus("AC").contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(10L)
                .verifyComplete();
    }

    @Test
    void save_WhenMunicipalityIdAlreadySet_ShouldOverrideWithContext() {
        UUID clientMunicipalityId = UUID.randomUUID();
        asset.setMunicipalityId(clientMunicipalityId);
        when(repository.save(any(Asset.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(adapter.save(asset).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNextMatches(saved -> saved.getMunicipalityId().equals(municipalityId))
                .verifyComplete();
    }

    @Test
    void deleteById_WhenNotFound_ShouldThrowException() {
        UUID id = asset.getId();
        when(repository.findByIdAndMunicipalityId(id, municipalityId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectError(RuntimeException.class)
                .verify();
    }
}

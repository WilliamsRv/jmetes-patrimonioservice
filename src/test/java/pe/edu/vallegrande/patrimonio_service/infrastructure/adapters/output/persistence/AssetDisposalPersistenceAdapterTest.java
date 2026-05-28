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

import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
class AssetDisposalPersistenceAdapterTest {

    @Mock
    private AssetDisposalRepository repository;

    @InjectMocks
    private AssetDisposalPersistenceAdapter adapter;

    private UUID municipalityId;
    private AssetDisposal disposal;

    @BeforeEach
    void setUp() {
        municipalityId = UUID.randomUUID();
        disposal = new AssetDisposal();
        disposal.setId(UUID.randomUUID());
        disposal.setMunicipalityId(municipalityId);
    }

    @Test
    void save_ShouldSetMunicipalityIdAndSave() {
        when(repository.save(any(AssetDisposal.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(adapter.save(disposal).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNextMatches(saved -> saved.getMunicipalityId().equals(municipalityId))
                .verifyComplete();
    }

    @Test
    void findById_WhenBelongsToMunicipality_ShouldReturnDisposal() {
        UUID id = disposal.getId();
        when(repository.findById(id)).thenReturn(Mono.just(disposal));

        StepVerifier.create(adapter.findById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(disposal)
                .verifyComplete();
    }

    @Test
    void findById_WhenBelongsToOtherMunicipality_ShouldReturnEmpty() {
        UUID id = disposal.getId();
        disposal.setMunicipalityId(UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Mono.just(disposal));

        StepVerifier.create(adapter.findById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .verifyComplete();
    }

    @Test
    void findAll_ShouldCallRepository() {
        when(repository.findByMunicipalityId(municipalityId)).thenReturn(Flux.just(disposal));

        StepVerifier.create(adapter.findAll().contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(disposal)
                .verifyComplete();
    }

    @Test
    void deleteById_WhenExistsAndBelongs_ShouldDelete() {
        UUID id = disposal.getId();
        when(repository.findById(id)).thenReturn(Mono.just(disposal));
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .verifyComplete();
    }

    @Test
    void deleteById_WhenNotBelongs_ShouldReturnError() {
        UUID id = disposal.getId();
        disposal.setMunicipalityId(UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Mono.just(disposal));

        StepVerifier.create(adapter.deleteById(id).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void findByFileStatus_ShouldCallRepository() {
        when(repository.findByFileStatusAndMunicipalityId("INITIATED", municipalityId)).thenReturn(Flux.just(disposal));

        StepVerifier.create(adapter.findByFileStatus("INITIATED").contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(disposal)
                .verifyComplete();
    }

    @Test
    void findByFileNumber_ShouldCallRepository() {
        when(repository.findByFileNumberAndMunicipalityId("EXP-001", municipalityId)).thenReturn(Mono.just(disposal));

        StepVerifier.create(adapter.findByFileNumber("EXP-001").contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(disposal)
                .verifyComplete();
    }

    @Test
    void findByRequestedBy_ShouldCallRepository() {
        UUID requestedBy = UUID.randomUUID();
        when(repository.findByRequestedByAndMunicipalityId(requestedBy, municipalityId)).thenReturn(Flux.just(disposal));

        StepVerifier.create(adapter.findByRequestedBy(requestedBy).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(disposal)
                .verifyComplete();
    }

    @Test
    void existsById_ShouldCallRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsById(id))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByFileNumber_ShouldCallRepository() {
        when(repository.existsByFileNumberAndMunicipalityId("EXP-001", municipalityId)).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsByFileNumber("EXP-001").contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(true)
                .verifyComplete();
    }
}

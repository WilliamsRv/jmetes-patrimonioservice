package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
class AssetDisposalDetailPersistenceAdapterTest {

    @Mock
    private AssetDisposalDetailRepository repository;

    @InjectMocks
    private AssetDisposalDetailPersistenceAdapter adapter;

    private UUID municipalityId;
    private AssetDisposalDetail detail;

    @BeforeEach
    void setUp() {
        municipalityId = UUID.randomUUID();
        detail = new AssetDisposalDetail();
        detail.setId(UUID.randomUUID());
        detail.setMunicipalityId(municipalityId);
    }

    @Test
    void save_ShouldSetMunicipalityIdAndSave() {
        when(repository.save(any(AssetDisposalDetail.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(adapter.save(detail).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNextMatches(saved -> saved.getMunicipalityId().equals(municipalityId))
                .verifyComplete();
    }

    @Test
    void findByDisposalId_ShouldFilterByMunicipalityId() {
        UUID disposalId = UUID.randomUUID();
        when(repository.findByDisposalId(disposalId)).thenReturn(Flux.just(detail));

        StepVerifier.create(adapter.findByDisposalId(disposalId).contextWrite(Context.of("municipalityId", municipalityId)))
                .expectNext(detail)
                .verifyComplete();
    }
}

package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AssetDisposalRepository extends ReactiveCrudRepository<AssetDisposal, UUID> {

        Mono<AssetDisposal> findByFileNumber(String fileNumber);

        Flux<AssetDisposal> findByFileStatus(String fileStatus);

        Flux<AssetDisposal> findByRequestedBy(UUID requestedBy);

        Flux<AssetDisposal> findByMunicipalityId(UUID municipalityId);

        Mono<Boolean> existsByFileNumber(String fileNumber);

        // Tenant-aware queries
        Mono<AssetDisposal> findByFileNumberAndMunicipalityId(String fileNumber, UUID municipalityId);

        Flux<AssetDisposal> findByFileStatusAndMunicipalityId(String fileStatus, UUID municipalityId);

        Flux<AssetDisposal> findByRequestedByAndMunicipalityId(UUID requestedBy, UUID municipalityId);

        Mono<Boolean> existsByFileNumberAndMunicipalityId(String fileNumber, UUID municipalityId);
}

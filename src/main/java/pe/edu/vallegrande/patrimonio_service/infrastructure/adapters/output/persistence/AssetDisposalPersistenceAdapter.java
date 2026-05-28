package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetDisposalPersistenceAdapter implements AssetDisposalPersistencePort {

    private final AssetDisposalRepository repository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetDisposalPersistenceAdapter.class);

    public AssetDisposalPersistenceAdapter(AssetDisposalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AssetDisposal> save(AssetDisposal assetDisposal) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> {
                    if (assetDisposal.getMunicipalityId() != null && !municipalityId.equals(assetDisposal.getMunicipalityId())) {
                        log.warn("Ignoring client-provided municipalityId={} and applying context municipalityId={} for disposal fileNumber={}", assetDisposal.getMunicipalityId(), municipalityId, assetDisposal.getFileNumber());
                    } else {
                        log.debug("Applying municipalityId={} from security context for disposal fileNumber={}", municipalityId, assetDisposal.getFileNumber());
                    }

                    assetDisposal.setMunicipalityId(municipalityId);
                    return repository.save(assetDisposal);
                });
    }

    @Override
    public Mono<AssetDisposal> findById(UUID id) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.findById(id)
                        .filter(ad -> municipalityId.equals(ad.getMunicipalityId())));
    }

    @Override
    public Flux<AssetDisposal> findAll() {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(repository::findByMunicipalityId);
    }

    @Override
    public Flux<AssetDisposal> findByFileStatus(String fileStatus) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(municipalityId -> repository.findByFileStatusAndMunicipalityId(fileStatus, municipalityId));
    }

    @Override
    public Mono<AssetDisposal> findByFileNumber(String fileNumber) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.findByFileNumberAndMunicipalityId(fileNumber, municipalityId));
    }

    @Override
    public Flux<AssetDisposal> findByRequestedBy(UUID requestedBy) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(municipalityId -> repository.findByRequestedByAndMunicipalityId(requestedBy, municipalityId));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> repository.findById(id)
                .filter(ad -> municipalityId.equals(ad.getMunicipalityId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Asset disposal not found or access denied")))
                .flatMap(ad -> repository.deleteById(id)));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByFileNumber(String fileNumber) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.existsByFileNumberAndMunicipalityId(fileNumber, municipalityId));
    }
}

package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetDisposalDetailPersistenceAdapter implements AssetDisposalDetailPersistencePort {

    private final AssetDisposalDetailRepository repository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetDisposalDetailPersistenceAdapter.class);

    public AssetDisposalDetailPersistenceAdapter(AssetDisposalDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AssetDisposalDetail> save(AssetDisposalDetail assetDisposalDetail) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> {
                    if (assetDisposalDetail.getMunicipalityId() != null && !municipalityId.equals(assetDisposalDetail.getMunicipalityId())) {
                        log.warn("Ignoring client-provided municipalityId={} and applying context municipalityId={} for disposal detail assetId={}", assetDisposalDetail.getMunicipalityId(), municipalityId, assetDisposalDetail.getAssetId());
                    } else {
                        log.debug("Applying municipalityId={} from security context for disposal detail assetId={}", municipalityId, assetDisposalDetail.getAssetId());
                    }

                    assetDisposalDetail.setMunicipalityId(municipalityId);
                    return repository.save(assetDisposalDetail);
                });
    }

    @Override
    public Mono<AssetDisposalDetail> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<AssetDisposalDetail> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMapMany(municipalityId -> repository.findByDisposalId(disposalId)
                .filter(detail -> municipalityId.equals(detail.getMunicipalityId())));
    }

    @Override
    public Flux<AssetDisposalDetail> findByAssetId(UUID assetId) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMapMany(municipalityId -> repository.findByAssetId(assetId)
                .filter(detail -> municipalityId.equals(detail.getMunicipalityId())));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByAssetIdInActiveDisposal(UUID assetId) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.existsByAssetIdInActiveDisposalAndMunicipalityId(assetId, municipalityId));
    }

    @Override
    public Mono<Boolean> existsByDisposalIdAndAssetId(UUID disposalId, UUID assetId) {
        return repository.findByDisposalId(disposalId)
                .filter(detail -> detail.getAssetId().equals(assetId))
                .hasElements();
    }

    @Override
    public Flux<UUID> findActiveAssetIds() {
        return repository.findActiveAssetIds();
    }
}

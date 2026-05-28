package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetPersistenceAdapter implements AssetPersistencePort {

    private final AssetRepository repository;
    private static final Logger log = LoggerFactory.getLogger(AssetPersistenceAdapter.class);

    public AssetPersistenceAdapter(AssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Asset> save(Asset asset) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> {
                    // Log if client provided a different municipalityId and we override it
                    if (asset.getMunicipalityId() != null && !municipalityId.equals(asset.getMunicipalityId())) {
                        log.warn("Ignoring client-provided municipalityId={} and applying context municipalityId={} for assetCode={}", asset.getMunicipalityId(), municipalityId, asset.getAssetCode());
                    } else {
                        log.debug("Applying municipalityId={} from security context for assetCode={}", municipalityId, asset.getAssetCode());
                    }

                    asset.setMunicipalityId(municipalityId);
                    return repository.save(asset);
                });
    }

    @Override
    public Mono<Asset> findById(UUID id) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> repository.findByIdAndMunicipalityId(id, municipalityId));
    }

    @Override
    public Flux<Asset> findAll() {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMapMany(repository::findAllByMunicipalityId);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> repository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new RuntimeException("Asset not found or access denied")))
                .flatMap(asset -> repository.deleteById(id)));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.findByIdAndMunicipalityId(id, municipalityId).hasElement());
    }

    @Override
    public Flux<Asset> findByAssetStatus(String status) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(municipalityId -> repository.findByAssetStatusAndMunicipalityId(status, municipalityId));
    }

    @Override
    public Mono<Asset> findByAssetCode(String assetCode) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.findByAssetCodeAndMunicipalityId(assetCode, municipalityId));
    }

    @Override
    public Flux<Asset> findByCurrentLocationId(UUID locationId) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(municipalityId -> repository.findByCurrentLocationIdAndMunicipalityId(locationId, municipalityId));
    }

    @Override
    public Flux<Asset> findByCurrentResponsibleId(UUID responsibleId) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMapMany(municipalityId -> repository.findByCurrentResponsibleIdAndMunicipalityId(responsibleId, municipalityId));
    }

    @Override
    public Mono<Long> countByAssetStatus(String status) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
                .flatMap(municipalityId -> repository.countByAssetStatusAndMunicipalityId(status, municipalityId));
    }

    @Override
    public Mono<String> findLastAssetCodeStartingWith(String prefix) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> {
                log.debug("Querying last asset code for prefix='{}' municipality={}", prefix, municipalityId);
                return repository.findTopAssetCodeByPrefixAndMunicipalityId(prefix, municipalityId)
                    .doOnNext(code -> log.debug("Found last asset code='{}' for prefix='{}' municipality={}", code, prefix, municipalityId))
                    .doOnSuccess(code -> { if (code == null) log.debug("No asset code found for prefix='{}' municipality={}", prefix, municipalityId); });
            });
    }
}

package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.DepreciationPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.DepreciationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class DepreciationPersistenceAdapter implements DepreciationPersistencePort {

    private static final String ACCESS_DENIED = "Asset not found or access denied";
    private static final String ACCESS_DENIED_FOR_DEPRECIATION = "Asset not found or access denied for depreciation";

    private final DepreciationRepository repository;
    private final pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort assetPersistencePort;

    public DepreciationPersistenceAdapter(DepreciationRepository repository,
                                         pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort assetPersistencePort) {
        this.repository = repository;
        this.assetPersistencePort = assetPersistencePort;
    }

    @Override
    public Mono<Depreciation> save(Depreciation depreciation) {
        // Ensure asset belongs to current tenant
        return assetPersistencePort.findById(depreciation.getAssetId())
            .switchIfEmpty(Mono.error(new RuntimeException(ACCESS_DENIED_FOR_DEPRECIATION)))
            .flatMap(a -> repository.save(depreciation));
    }

    @Override
    public Mono<Depreciation> findById(UUID id) {
        return repository.findById(id)
            .flatMap(dep -> assetPersistencePort.findById(dep.getAssetId())
                .switchIfEmpty(Mono.error(new RuntimeException(ACCESS_DENIED_FOR_DEPRECIATION)))
                .thenReturn(dep));
    }

    @Override
    public Flux<Depreciation> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<Depreciation> findByAssetId(UUID assetId) {
        return assetPersistencePort.findById(assetId)
                .switchIfEmpty(Mono.error(new RuntimeException(ACCESS_DENIED)))
                .flatMapMany(a -> repository.findByAssetId(assetId));
    }

    @Override
    public Flux<Depreciation> findByFiscalYear(Integer fiscalYear) {
        return repository.findByFiscalYear(fiscalYear);
    }

    @Override
    public Mono<Depreciation> findByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth) {
        return repository.findByAssetIdAndFiscalYearAndCalculationMonth(assetId, fiscalYear, calculationMonth);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth) {
        return assetPersistencePort.findById(assetId)
            .switchIfEmpty(Mono.error(new RuntimeException(ACCESS_DENIED)))
            .thenMany(repository.findByAssetId(assetId))
            .filter(dep -> dep.getFiscalYear().equals(fiscalYear) && dep.getCalculationMonth().equals(calculationMonth))
            .flatMap(repository::delete)
            .then();
    }

    @Override
    public Mono<Void> deleteByAssetId(UUID assetId) {
        return assetPersistencePort.findById(assetId)
            .switchIfEmpty(Mono.error(new RuntimeException(ACCESS_DENIED)))
            .then(repository.deleteByAssetId(assetId))
            .then();
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }
}

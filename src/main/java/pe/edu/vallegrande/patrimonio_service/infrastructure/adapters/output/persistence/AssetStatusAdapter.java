package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetStatusPort;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adaptador que implementa el puerto de estado de activos usando el repositorio existente.
 * Mantiene la misma lógica que estaba en el servicio pero desacoplado.
 */
@Component
public class AssetStatusAdapter implements AssetStatusPort {

    private final AssetRepository assetRepository;

    public AssetStatusAdapter(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public Mono<Void> updateAssetStatusToDisposed(UUID assetId, LocalDateTime updatedAt) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> assetRepository.updateAssetStatus(assetId, "DISPOSED", updatedAt, municipalityId))
            .then();
    }

    @Override
    public Mono<Void> updateAssetStatusToAvailable(UUID assetId, LocalDateTime updatedAt) {
        return pe.edu.vallegrande.patrimonio_service.infrastructure.security.TenantSecurityContext.currentMunicipalityId()
            .flatMap(municipalityId -> assetRepository.updateAssetStatus(assetId, "AVAILABLE", updatedAt, municipalityId))
            .then();
    }
}

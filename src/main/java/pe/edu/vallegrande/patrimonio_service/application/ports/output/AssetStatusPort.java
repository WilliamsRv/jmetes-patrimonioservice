package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de estado de activos.
 * Abstrae la actualización de estado de los assets del repositorio concreto.
 */
public interface AssetStatusPort {
    
    /**
     * Actualiza el estado de un activo a DISPUESTO (BAJA)
     */
    Mono<Void> updateAssetStatusToDisposed(UUID assetId, LocalDateTime updatedAt);
    
    /**
     * Actualiza el estado de un activo a DISPONIBLE (AVAILABLE)
     */
    Mono<Void> updateAssetStatusToAvailable(UUID assetId, LocalDateTime updatedAt);
}

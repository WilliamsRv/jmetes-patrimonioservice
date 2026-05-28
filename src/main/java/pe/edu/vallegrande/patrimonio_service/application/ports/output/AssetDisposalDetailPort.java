package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de detalles de disposición de activos.
 * Abstrae el acceso a datos de AssetDisposalDetail del repositorio concreto.
 */
public interface AssetDisposalDetailPort {
    
    /**
     * Busca todos los detalles de un expediente de disposición
     */
    Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId);
    
    /**
     * Busca un activo por su ID (necesario para registro de movimientos)
     */
    Mono<Asset> findAssetById(UUID assetId);
}

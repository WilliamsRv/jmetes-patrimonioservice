package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPort;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Adaptador que implementa el puerto de detalles de disposición usando los repositorios existentes.
 * Mantiene la misma lógica que estaba en el servicio pero desacoplado.
 */
@Component
public class AssetDisposalDetailAdapter implements AssetDisposalDetailPort {

    private final AssetDisposalDetailRepository disposalDetailRepository;
    private final AssetRepository assetRepository;

    public AssetDisposalDetailAdapter(AssetDisposalDetailRepository disposalDetailRepository,
                                    AssetRepository assetRepository) {
        this.disposalDetailRepository = disposalDetailRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId) {
        return disposalDetailRepository.findByDisposalId(disposalId);
    }

    @Override
    public Mono<Asset> findAssetById(UUID assetId) {
        return assetRepository.findById(assetId);
    }
}

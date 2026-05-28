package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import java.util.UUID;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetUseCase {

    Mono<AssetResponse> create(AssetRequest request);

    Mono<AssetResponse> getById(UUID id);

    Flux<AssetResponse> getAll();

    Mono<AssetResponse> update(UUID id, AssetRequest request);

    Mono<Void> delete(UUID id);

    Mono<AssetResponse> changeStatus(UUID id, CambioEstadoRequest request);

    Flux<AssetResponse> findByStatus(String status);

    Mono<AssetResponse> findByAssetCode(String assetCode);

    // Create multiple assets in a single transactional operation
    Flux<AssetResponse> createBatch(Flux<AssetRequest> requests);

    // Return the latest full assetCode that starts with the given SBN prefix within the tenant
    Mono<String> findLastAssetCodeStartingWith(String sbnCode);

        // Calculate next sequence for a given SBN prefix
        reactor.core.publisher.Mono<pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse> findNextSequence(String sbnCode);
}

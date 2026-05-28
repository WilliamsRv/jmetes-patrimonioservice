package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AssetRepository extends ReactiveCrudRepository<Asset, UUID> {
    Mono<Asset> findByAssetCode(String assetCode);

    Flux<Asset> findByAssetStatus(String assetStatus);

    Flux<Asset> findByCurrentLocationId(UUID locationId);

    Flux<Asset> findByCurrentResponsibleId(UUID responsibleId);

    Mono<Long> countByAssetStatus(String assetStatus);

    // Tenant-aware queries
    Flux<Asset> findAllByMunicipalityId(UUID municipalityId);

    Mono<Asset> findByIdAndMunicipalityId(UUID id, UUID municipalityId);

    Mono<Asset> findByAssetCodeAndMunicipalityId(String assetCode, UUID municipalityId);

    Flux<Asset> findByAssetStatusAndMunicipalityId(String assetStatus, UUID municipalityId);

    Flux<Asset> findByCurrentLocationIdAndMunicipalityId(UUID locationId, UUID municipalityId);

    Flux<Asset> findByCurrentResponsibleIdAndMunicipalityId(UUID responsibleId, UUID municipalityId);

    Mono<Long> countByAssetStatusAndMunicipalityId(String assetStatus, UUID municipalityId);

    @Query("SELECT asset_code FROM assets WHERE asset_code LIKE CONCAT(:prefix, '%') AND municipality_id = :municipalityId ORDER BY asset_code DESC LIMIT 1")
    Mono<String> findTopAssetCodeByPrefixAndMunicipalityId(String prefix, UUID municipalityId);

    @Query("UPDATE assets SET asset_status = :status, updated_at = :updatedAt WHERE id = :id AND municipality_id = :municipalityId")
    Mono<Integer> updateAssetStatus(UUID id, String status, LocalDateTime updatedAt, UUID municipalityId);
}

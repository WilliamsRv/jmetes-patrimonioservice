package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.DepreciationUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.SbnCatalogPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetAlreadyExistsException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AssetService implements AssetUseCase {

    private final AssetPersistencePort persistencePort;
    private final SbnCatalogPersistencePort sbnCatalogPersistencePort;
    private final DepreciationUseCase depreciationUseCase;

    public AssetService(AssetPersistencePort persistencePort, SbnCatalogPersistencePort sbnCatalogPersistencePort, DepreciationUseCase depreciationUseCase) {
        this.persistencePort = persistencePort;
        this.sbnCatalogPersistencePort = sbnCatalogPersistencePort;
        this.depreciationUseCase = depreciationUseCase;
    }

    @Override
    public Mono<AssetResponse> create(AssetRequest request) {
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);

        // Set default values
        asset.setCreatedAt(LocalDateTime.now());

        // If created_by is not provided, use a default UUID
        if (asset.getCreatedBy() == null) {
            asset.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }

        // Initial monetary values
        if (asset.getCurrentValue() == null) {
            asset.setCurrentValue(asset.getAcquisitionValue());
        }

        // Validate sbnCode exists in catalog if provided
        Mono<Void> sbnValidation = Mono.justOrEmpty(asset.getSbnCode())
                .flatMap(sbnCode -> sbnCatalogPersistencePort.findByCodigo(sbnCode)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("El código SBN " + sbnCode + " no existe en el catálogo SBN")))
                        .then());

        // Validate uniqueness of asset_code within tenant
        return sbnValidation.then(
            persistencePort.findByAssetCode(asset.getAssetCode())
                .flatMap(existing -> Mono.<Asset>error(new AssetAlreadyExistsException("El código patrimonial " + asset.getAssetCode() + " ya está registrado")))
                .switchIfEmpty(persistencePort.save(asset))
                .flatMap(saved -> {
                    boolean isDepreciable = saved.getIsDepreciable() != null && saved.getIsDepreciable();
                    boolean hasUsefulLife = saved.getUsefulLife() != null && saved.getUsefulLife() > 0;
                    if (isDepreciable && hasUsefulLife) {
                        return depreciationUseCase.createInitialDepreciation(saved)
                                .thenReturn(saved);
                    }
                    return Mono.just(saved);
                })
                .map(this::convertToResponse)
        );
    }

    @Override
    @Transactional
    public Flux<AssetResponse> createBatch(Flux<AssetRequest> requests) {
        return requests
                .flatMap(request -> {
                    Asset asset = new Asset();
                    BeanUtils.copyProperties(request, asset);
                    asset.setCreatedAt(LocalDateTime.now());

                    if (asset.getCreatedBy() == null) {
                        asset.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                    }

                    if (asset.getCurrentValue() == null) {
                        asset.setCurrentValue(asset.getAcquisitionValue());
                    }

                    return persistencePort.findByAssetCode(asset.getAssetCode())
                            .flatMap(existing -> Mono.<Asset>error(new AssetAlreadyExistsException("El código patrimonial " + asset.getAssetCode() + " ya está registrado")))
                            .switchIfEmpty(persistencePort.save(asset));
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<String> findLastAssetCodeStartingWith(String sbnCode) {
        return persistencePort.findLastAssetCodeStartingWith(sbnCode)
                .defaultIfEmpty("");
    }

    @Override
    public Mono<pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse> findNextSequence(String sbnCode) {
        return persistencePort.findLastAssetCodeStartingWith(sbnCode)
                .flatMap(code -> {
                    pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse resp = new pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse();
                    String lastCode = code;
                    String suffix = "";
                    Integer parsed = null;
                    String nextSeq = "001";

                    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetService.class);
                    log.debug("findNextSequence - sbnCode='{}' lastCode='{}'", sbnCode, lastCode);

                    if (lastCode == null || lastCode.isEmpty()) {
                        resp.setLastCode(null);
                        resp.setNextSeq(nextSeq);
                        resp.setFullAssetCode(sbnCode + "-" + nextSeq);
                        log.debug("findNextSequence - no lastCode found, returning nextSeq={}", nextSeq);
                        return Mono.just(resp);
                    }

                    resp.setLastCode(lastCode);

                    // Prefer substring when lastCode starts with sbnCode
                    if (lastCode.startsWith(sbnCode)) {
                        try {
                            suffix = lastCode.substring(sbnCode.length()).replaceAll("^\\D+", "");
                        } catch (Exception e) {
                            log.warn("findNextSequence - substring failed for lastCode='{}' sbnCode='{}'", lastCode, sbnCode, e);
                            suffix = "";
                        }
                    } else {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)$");
                        java.util.regex.Matcher m = p.matcher(lastCode);
                        if (m.find()) suffix = m.group(1);
                    }

                    try {
                        if (suffix != null && !suffix.isEmpty()) {
                            parsed = Integer.parseInt(suffix);
                            int next = parsed + 1;
                            // preserve width of suffix when formatting
                            nextSeq = String.format("%0" + suffix.length() + "d", next);
                        } else {
                            nextSeq = "001";
                        }
                    } catch (Exception ex) {
                        org.slf4j.LoggerFactory.getLogger(AssetService.class).warn("findNextSequence - failed to parse suffix='{}' from lastCode='{}' for sbnCode='{}'. Fallback to 001", suffix, lastCode, sbnCode, ex);
                        nextSeq = "001";
                    }

                    org.slf4j.LoggerFactory.getLogger(AssetService.class).debug("findNextSequence - sbnCode='{}' lastCode='{}' suffix='{}' parsed='{}' nextSeq='{}'", sbnCode, lastCode, suffix, parsed, nextSeq);
                    resp.setNextSeq(nextSeq);
                    resp.setFullAssetCode(sbnCode + "-" + nextSeq);
                    return Mono.just(resp);
                })
                .defaultIfEmpty(createDefaultNextSeqResponse(sbnCode));
    }

    @Override
    public Mono<AssetResponse> getById(UUID id) {
        return persistencePort.findById(id)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)));
    }

    @Override
    public Flux<AssetResponse> getAll() {
        return persistencePort.findAll()
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetResponse> update(UUID id, AssetRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(existing -> {
                    boolean depreciationChanged = hasDepreciationFieldsChanged(existing, request);

                    BeanUtils.copyProperties(request, existing, "id", "createdBy", "createdAt");
                    existing.setUpdatedAt(LocalDateTime.now());

                    if (depreciationChanged && existing.getIsDepreciable() != null && existing.getIsDepreciable()) {
                        return persistencePort.save(existing)
                                .flatMap(saved -> depreciationUseCase.recalculateForAsset(saved.getId())
                                        .thenReturn(saved))
                                .map(this::convertToResponse);
                    }

                    return persistencePort.save(existing)
                            .map(this::convertToResponse);
                });
    }

    private boolean hasDepreciationFieldsChanged(Asset existing, AssetRequest request) {
        if (request.getAcquisitionDate() != null && !request.getAcquisitionDate().equals(existing.getAcquisitionDate()))
            return true;
        if (request.getAcquisitionValue() != null && (existing.getAcquisitionValue() == null
                || request.getAcquisitionValue().compareTo(existing.getAcquisitionValue()) != 0))
            return true;
        if (request.getResidualValue() != null && (existing.getResidualValue() == null
                || request.getResidualValue().compareTo(existing.getResidualValue()) != 0))
            return true;
        return request.getUsefulLife() != null && !request.getUsefulLife().equals(existing.getUsefulLife());
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(asset -> persistencePort.deleteById(id));
    }

    @Override
    public Mono<AssetResponse> changeStatus(UUID id, CambioEstadoRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(asset -> {
                    asset.setAssetStatus(request.getNuevoEstado());
                    asset.setObservations(request.getObservaciones());
                    asset.setUpdatedAt(LocalDateTime.now());
                    
                    if (request.getModificadoPor() != null) {
                        asset.setUpdatedBy(request.getModificadoPor());
                    }
                    
                    return persistencePort.save(asset);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Flux<AssetResponse> findByStatus(String status) {
        return persistencePort.findByAssetStatus(status)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetResponse> findByAssetCode(String assetCode) {
        return persistencePort.findByAssetCode(assetCode)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with code: " + assetCode)));
    }

    private NextSeqResponse createDefaultNextSeqResponse(String sbnCode) {
        NextSeqResponse resp = new NextSeqResponse();
        resp.setLastCode(null);
        resp.setNextSeq("001");
        resp.setFullAssetCode(sbnCode + "-001");
        return resp;
    }

    private AssetResponse convertToResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        BeanUtils.copyProperties(asset, response);
        return response;
    }
}

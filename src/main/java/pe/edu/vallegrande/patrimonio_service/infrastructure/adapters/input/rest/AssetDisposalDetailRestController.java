package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalDetailUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/asset-disposal-details")
@RequiredArgsConstructor
public class AssetDisposalDetailRestController {

    private final AssetDisposalDetailUseCase assetDisposalDetailUseCase;

    @PostMapping
    public Mono<AssetDisposalDetailResponse> create(@RequestBody AssetDisposalDetailRequest request) {
        log.info("[MS-API] POST /api/v1/asset-disposal-details");
        return assetDisposalDetailUseCase.create(request)
                .doOnError(error -> {
                    log.error("[MS-API] Error creating asset disposal detail: {}", error.getMessage());
                });
    }

    @GetMapping("/{id}")
    public Mono<AssetDisposalDetailResponse> getById(@PathVariable UUID id) {
        log.info("[MS-API] GET /api/v1/asset-disposal-details/{}", id);
        return assetDisposalDetailUseCase.getById(id);
    }

    @GetMapping("/disposal/{disposalId}")
    public Flux<AssetDisposalDetailResponse> getByDisposalId(@PathVariable UUID disposalId) {
        return assetDisposalDetailUseCase.getByDisposalId(disposalId);
    }

    @GetMapping("/asset/{assetId}")
    public Flux<AssetDisposalDetailResponse> getByAssetId(@PathVariable UUID assetId) {
        return assetDisposalDetailUseCase.getByAssetId(assetId);
    }

    @PutMapping("/{id}/technical-opinion")
    public Mono<AssetDisposalDetailResponse> addTechnicalOpinion(
            @PathVariable UUID id,
            @RequestBody TechnicalOpinionRequest request) {
        return assetDisposalDetailUseCase.addTechnicalOpinion(id, request);
    }

    @PutMapping("/{id}/execute-removal")
    public Mono<AssetDisposalDetailResponse> executeRemoval(
            @PathVariable UUID id,
            @RequestBody ExecuteRemovalRequest request) {
        return assetDisposalDetailUseCase.executeRemoval(id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetDisposalDetailUseCase.delete(id);
    }

    @GetMapping("/active-asset-ids")
    public Flux<UUID> getActiveAssetIds() {
        return assetDisposalDetailUseCase.findActiveAssetIds();
    }
}

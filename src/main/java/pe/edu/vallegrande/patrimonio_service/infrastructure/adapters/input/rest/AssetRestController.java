package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import java.util.List;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
public class AssetRestController {

    private final AssetUseCase assetUseCase;

    public AssetRestController(AssetUseCase assetUseCase) {
        this.assetUseCase = assetUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AssetResponse> create(@RequestBody AssetRequest request) {
        log.info("[MS-API] POST /api/v1/assets");
        return assetUseCase.create(request);
    }

    @PostMapping("/batch")
    public Mono<ResponseEntity<List<AssetResponse>>> createBatch(@RequestBody Mono<List<AssetRequest>> requestsMono) {
        return requestsMono
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ServerWebInputException("No request body")))
                .flatMap(list -> assetUseCase.createBatch(Flux.fromIterable(list))
                        .collectList()
                        .map(responses -> ResponseEntity.status(HttpStatus.CREATED).body(responses))
                );
    }

    @GetMapping("/{id}")
    public Mono<AssetResponse> getById(@PathVariable UUID id) {
        return assetUseCase.getById(id);
    }

    @GetMapping
    public Flux<AssetResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("[MS-API] GET /api/v1/assets");
        return assetUseCase.getAll();
    }

    @PutMapping("/{id}")
    public Mono<AssetResponse> update(@PathVariable UUID id, @RequestBody AssetRequest request) {
        return assetUseCase.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetUseCase.delete(id);
    }

    @PatchMapping("/{id}/status")
    public Mono<AssetResponse> changeStatus(@PathVariable UUID id, @RequestBody CambioEstadoRequest request) {
        return assetUseCase.changeStatus(id, request);
    }

    @GetMapping("/status/{status}")
    public Flux<AssetResponse> findByStatus(@PathVariable String status) {
        return assetUseCase.findByStatus(status);
    }

    @GetMapping("/code/{assetCode}")
    public Mono<AssetResponse> findByAssetCode(@PathVariable String assetCode) {
        return assetUseCase.findByAssetCode(assetCode);
    }

    @GetMapping("/last-code/{sbnCode}")
    public Mono<org.springframework.http.ResponseEntity<String>> findLastCode(@PathVariable String sbnCode) {
        return assetUseCase.findLastAssetCodeStartingWith(sbnCode)
                .map(code -> org.springframework.http.ResponseEntity.ok(code))
                .defaultIfEmpty(org.springframework.http.ResponseEntity.ok(""))
                .onErrorResume(throwable -> Mono.just(org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body("")));
    }

    @GetMapping("/validate-sbn/{sbnCode}")
    public Mono<pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse> validateSbnCode(
            @PathVariable String sbnCode) {
        return assetUseCase.findLastAssetCodeStartingWith(sbnCode)
                .flatMap(lastCode -> {
                    if (lastCode == null || lastCode.isEmpty()) {
                        return Mono.just(new pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse(false, null, null));
                    }
                    return assetUseCase.findByAssetCode(lastCode)
                            .map(asset -> new pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse(
                                    true, asset.getAssetCode(), asset.getDescription()))
                            .switchIfEmpty(Mono.just(new pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse(false, null, null)));
                })
                .defaultIfEmpty(new pe.edu.vallegrande.patrimonio_service.application.dto.SBNValidationResponse(false, null, null));
    }

    @GetMapping("/next-seq/{sbnCode}")
    public Mono<org.springframework.http.ResponseEntity<pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse>> nextSeq(@PathVariable String sbnCode) {
        return assetUseCase.findNextSequence(sbnCode)
                .map(resp -> org.springframework.http.ResponseEntity.ok(resp))
                .onErrorResume(throwable -> {
                    org.slf4j.LoggerFactory.getLogger(AssetRestController.class).debug("Error computing next-seq for {}: {}", sbnCode, throwable.toString());
                    return Mono.just(org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(new pe.edu.vallegrande.patrimonio_service.application.dto.NextSeqResponse()));
                });
    }
}

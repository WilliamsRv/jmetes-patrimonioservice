package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-disposals")
@RequiredArgsConstructor
public class AssetDisposalRestController {

    private final AssetDisposalUseCase assetDisposalUseCase;

    @PostMapping
    public Mono<AssetDisposalResponse> create(@RequestBody AssetDisposalRequest request) {
        return assetDisposalUseCase.create(request);
    }

    @GetMapping("/{id}")
    public Mono<AssetDisposalResponse> getById(@PathVariable UUID id) {
        return assetDisposalUseCase.getById(id);
    }

    @GetMapping
    public Flux<AssetDisposalResponse> getAll() {
        return assetDisposalUseCase.getAll();
    }

    @GetMapping("/status/{status}")
    public Flux<AssetDisposalResponse> getByStatus(@PathVariable String status) {
        return assetDisposalUseCase.getByStatus(status);
    }

    @GetMapping("/file-number/{fileNumber}")
    public Mono<AssetDisposalResponse> getByFileNumber(@PathVariable String fileNumber) {
        return assetDisposalUseCase.getByFileNumber(fileNumber);
    }

    @GetMapping("/requested-by/{userId}")
    public Flux<AssetDisposalResponse> getByRequestedBy(@PathVariable UUID userId) {
        return assetDisposalUseCase.getByRequestedBy(userId);
    }

    @PutMapping("/{id}/assign-committee")
    public Mono<AssetDisposalResponse> assignCommittee(
            @PathVariable UUID id,
            @RequestBody AssignCommitteeRequest request) {
        return assetDisposalUseCase.assignCommittee(id, request);
    }

    @PutMapping("/{id}/resolve")
    public Mono<AssetDisposalResponse> resolve(
            @PathVariable UUID id,
            @RequestBody ResolveDisposalRequest request) {
        return assetDisposalUseCase.resolve(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public Mono<AssetDisposalResponse> cancel(
            @PathVariable UUID id,
            @RequestParam UUID cancelledBy) {
        return assetDisposalUseCase.cancel(id, cancelledBy);
    }

    @PatchMapping("/{id}/complete")
    public Mono<AssetDisposalResponse> completeAssetDisposal(@PathVariable UUID id) {
        return assetDisposalUseCase.completeAssetDisposal(id);
    }

    @PatchMapping("/{id}/restore")
    public Mono<AssetDisposalResponse> restore(@PathVariable UUID id) {
        return assetDisposalUseCase.restore(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetDisposalUseCase.delete(id);
    }
}

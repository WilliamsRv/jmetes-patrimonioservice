package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.dto.SbnCatalogResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.SbnCatalogUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/assets/sbn-catalog")
public class SbnCatalogRestController {

    private final SbnCatalogUseCase sbnCatalogUseCase;

    public SbnCatalogRestController(SbnCatalogUseCase sbnCatalogUseCase) {
        this.sbnCatalogUseCase = sbnCatalogUseCase;
    }

    @GetMapping
    public Flux<SbnCatalogResponse> getAll() {
        return sbnCatalogUseCase.getAll();
    }

    @GetMapping("/grupos")
    public Flux<String> getGrupos() {
        return sbnCatalogUseCase.getGrupos();
    }

    @GetMapping("/grupo/{grupo}")
    public Flux<SbnCatalogResponse> getByGrupo(@PathVariable String grupo) {
        return sbnCatalogUseCase.getByGrupo(grupo);
    }

    @GetMapping("/{codigo}")
    public Mono<SbnCatalogResponse> getByCodigo(@PathVariable String codigo) {
        return sbnCatalogUseCase.getByCodigo(codigo);
    }
}

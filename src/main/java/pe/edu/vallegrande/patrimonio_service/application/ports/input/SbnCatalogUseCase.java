package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import pe.edu.vallegrande.patrimonio_service.application.dto.SbnCatalogResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SbnCatalogUseCase {
    Flux<SbnCatalogResponse> getAll();
    Mono<SbnCatalogResponse> getByCodigo(String codigo);
    Flux<String> getGrupos();
    Flux<SbnCatalogResponse> getByGrupo(String grupo);
}

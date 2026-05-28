package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SbnCatalogPersistencePort {
    Flux<SbnCatalog> findAll();
    Mono<SbnCatalog> findByCodigo(String codigo);
    Flux<String> findDistinctGrupos();
    Flux<SbnCatalog> findByGrupo(String grupo);
}

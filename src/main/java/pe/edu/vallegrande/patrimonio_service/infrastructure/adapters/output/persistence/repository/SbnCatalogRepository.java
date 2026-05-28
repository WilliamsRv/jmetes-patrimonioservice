package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SbnCatalogRepository extends ReactiveCrudRepository<SbnCatalog, UUID> {
    Mono<SbnCatalog> findByCodigo(String codigo);

    @Query("SELECT DISTINCT grupo FROM sbn_catalog ORDER BY grupo")
    Flux<String> findDistinctGrupos();

    Flux<SbnCatalog> findByGrupo(String grupo);
}

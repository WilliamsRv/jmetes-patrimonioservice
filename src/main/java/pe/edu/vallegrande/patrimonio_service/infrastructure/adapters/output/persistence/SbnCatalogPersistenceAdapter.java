package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.SbnCatalogPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.SbnCatalogRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SbnCatalogPersistenceAdapter implements SbnCatalogPersistencePort {

    private final SbnCatalogRepository repository;

    public SbnCatalogPersistenceAdapter(SbnCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<SbnCatalog> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<SbnCatalog> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    @Override
    public Flux<String> findDistinctGrupos() {
        return repository.findDistinctGrupos();
    }

    @Override
    public Flux<SbnCatalog> findByGrupo(String grupo) {
        return repository.findByGrupo(grupo);
    }
}

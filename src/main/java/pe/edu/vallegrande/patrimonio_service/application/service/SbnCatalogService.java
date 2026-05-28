package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.dto.SbnCatalogResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.SbnCatalogUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.SbnCatalogPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SbnCatalogService implements SbnCatalogUseCase {

    private final SbnCatalogPersistencePort persistencePort;

    public SbnCatalogService(SbnCatalogPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Flux<SbnCatalogResponse> getAll() {
        return persistencePort.findAll().map(this::toResponse);
    }

    @Override
    public Mono<SbnCatalogResponse> getByCodigo(String codigo) {
        return persistencePort.findByCodigo(codigo)
                .map(this::toResponse);
    }

    @Override
    public Flux<String> getGrupos() {
        return persistencePort.findDistinctGrupos();
    }

    @Override
    public Flux<SbnCatalogResponse> getByGrupo(String grupo) {
        return persistencePort.findByGrupo(grupo).map(this::toResponse);
    }

    private SbnCatalogResponse toResponse(SbnCatalog catalog) {
        SbnCatalogResponse resp = new SbnCatalogResponse();
        resp.setCodigo(catalog.getCodigo());
        resp.setDescripcion(catalog.getDescripcion());
        resp.setGrupo(catalog.getGrupo());
        resp.setClase(catalog.getClase());
        resp.setTasaDepreciacionAnual(catalog.getTasaDepreciacionAnual());
        resp.setVidaUtilMeses(catalog.getVidaUtilMeses());
        resp.setEsDepreciable(catalog.getEsDepreciable());
        resp.setRequiereSerieMarcaModelo(catalog.getRequiereSerieMarcaModelo());
        return resp;
    }
}

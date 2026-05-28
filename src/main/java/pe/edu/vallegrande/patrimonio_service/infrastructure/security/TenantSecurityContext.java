package pe.edu.vallegrande.patrimonio_service.infrastructure.security;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.UUID;

public class TenantSecurityContext {

    private static final UUID DEFAULT_MUNICIPALITY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String MUNICIPALITY_CONTEXT_KEY = "municipalityId";

    private TenantSecurityContext() {}

    public static Mono<UUID> currentMunicipalityId() {
        return Mono.deferContextual((ContextView ctx) -> {
            if (ctx.hasKey(MUNICIPALITY_CONTEXT_KEY)) {
                Object v = ctx.get(MUNICIPALITY_CONTEXT_KEY);
                if (v instanceof UUID) {
                    return Mono.just((UUID) v);
                }
                try {
                    return Mono.just(UUID.fromString(v.toString()));
                } catch (IllegalArgumentException ex) {
                    return Mono.just(DEFAULT_MUNICIPALITY_ID);
                }
            }
            return Mono.just(DEFAULT_MUNICIPALITY_ID);
        });
    }
}

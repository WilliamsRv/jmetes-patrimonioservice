package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AssetStatusAdapterTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetStatusAdapter assetStatusAdapter;

    @Test
    void updateAssetStatusToDisposed_ShouldInvokeRepository() {
        UUID assetId = UUID.randomUUID();
        UUID municipalityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(assetRepository.updateAssetStatus(eq(assetId), eq("DISPOSED"), any(LocalDateTime.class), eq(municipalityId)))
                .thenReturn(Mono.just(1));

        StepVerifier.create(assetStatusAdapter.updateAssetStatusToDisposed(assetId, now)
                .contextWrite(ctx -> ctx.put("municipalityId", municipalityId.toString())))
                .verifyComplete();
    }

    @Test
    void updateAssetStatusToAvailable_ShouldInvokeRepository() {
        UUID assetId = UUID.randomUUID();
        UUID municipalityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(assetRepository.updateAssetStatus(eq(assetId), eq("AVAILABLE"), any(LocalDateTime.class), eq(municipalityId)))
                .thenReturn(Mono.just(1));

        StepVerifier.create(assetStatusAdapter.updateAssetStatusToAvailable(assetId, now)
                .contextWrite(ctx -> ctx.put("municipalityId", municipalityId.toString())))
                .verifyComplete();
    }
}

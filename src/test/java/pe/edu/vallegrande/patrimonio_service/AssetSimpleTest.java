package pe.edu.vallegrande.patrimonio_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.service.AssetService;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetSimpleTest - Pruebas unitarias rápidas de AssetService (create / findByAssetCode / delete)")
class AssetSimpleTest {

    @Mock
    private AssetPersistencePort persistencePort;

    @InjectMocks
    private AssetService assetService;

    private UUID assetId;
    private Asset asset;
    private AssetRequest assetRequest;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();

        asset = new Asset();
        asset.setId(assetId);
        asset.setAssetCode("ACT-001");
        asset.setDescription("Laptop Dell Latitude");
        asset.setAcquisitionValue(new BigDecimal("3500.00"));
        asset.setCurrentValue(new BigDecimal("3500.00"));

        assetRequest = new AssetRequest();
        assetRequest.setAssetCode("ACT-001");
        assetRequest.setDescription("Laptop Dell Latitude");
        assetRequest.setAcquisitionValue(new BigDecimal("3500.00"));
    }

    @Test
    @DisplayName("create: Crea un activo correctamente cuando los datos son válidos")
    void crearActivo_CuandoDatosValidos_RetornaActivoCreado() {
        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(asset));

        StepVerifier.create(assetService.create(assetRequest))
                .assertNext(response -> {
                    assertEquals("ACT-001", response.getAssetCode());
                    assertEquals("Laptop Dell Latitude", response.getDescription());
                    assertEquals(new BigDecimal("3500.00"), response.getAcquisitionValue());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findByAssetCode: Retorna el activo cuando el código existe")
    void buscarPorCodigo_CuandoExiste_RetornaActivo() {
        when(persistencePort.findByAssetCode("ACT-001")).thenReturn(Mono.just(asset));

        StepVerifier.create(assetService.findByAssetCode("ACT-001"))
                .assertNext(response -> {
                    assertEquals("ACT-001", response.getAssetCode());
                    assertEquals(assetId, response.getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("delete: Elimina un activo sin errores cuando existe")
    void eliminarActivo_CuandoExiste_EliminaSinErrores() {
        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.deleteById(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.delete(assetId))
                .verifyComplete();

        verify(persistencePort).deleteById(assetId);
    }
}

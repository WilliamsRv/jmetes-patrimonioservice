package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.DepreciationUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.SbnCatalogPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetPersistencePort persistencePort;

    @Mock
    private SbnCatalogPersistencePort sbnCatalogPersistencePort;

    @Mock
    private DepreciationUseCase depreciationUseCase;

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
        asset.setAssetCode("AST-001");
        asset.setDescription("Laptop Dell Latitude");
        asset.setAcquisitionValue(new BigDecimal("3500.00"));
        asset.setCurrentValue(new BigDecimal("3500.00"));
        asset.setAssetStatus("AC");

        assetRequest = new AssetRequest();
        assetRequest.setAssetCode("AST-001");
        assetRequest.setDescription("Laptop Dell Latitude");
        assetRequest.setAcquisitionValue(new BigDecimal("3500.00"));
    }

    @Test
    void create_WhenValidRequest_ShouldReturnSavedAsset() {
        // Arrange
        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.create(assetRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(asset.getAssetCode(), response.getAssetCode());
                    assertEquals(asset.getAcquisitionValue(), response.getAcquisitionValue());
                })
                .verifyComplete();

        verify(persistencePort, times(1)).save(any(Asset.class));
    }

    @Test
    void create_WhenValidRequest_ShouldSetDefaultValues() {
        AssetRequest request = new AssetRequest();
        request.setAssetCode("AST-DEFAULT");
        request.setAcquisitionValue(new BigDecimal("5000.00"));

        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(assetService.create(request))
                .assertNext(response -> {
                    assertEquals(new BigDecimal("5000.00"), response.getCurrentValue());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), response.getCreatedBy());
                })
                .verifyComplete();

        verify(persistencePort).save(argThat(asset ->
            asset.getCurrentValue().equals(new BigDecimal("5000.00")) &&
            asset.getCreatedBy().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        ));
    }

    @Test
    void create_WhenDepreciableWithUsefulLife_ShouldCreateInitialDepreciation() {
        AssetRequest request = new AssetRequest();
        request.setAssetCode("DEP-001");
        request.setAcquisitionValue(new BigDecimal("12000.00"));
        request.setIsDepreciable(true);
        request.setUsefulLife(60);

        Asset saved = new Asset();
        saved.setId(UUID.randomUUID());
        saved.setAssetCode("DEP-001");
        saved.setAcquisitionValue(new BigDecimal("12000.00"));
        saved.setCurrentValue(new BigDecimal("12000.00"));
        saved.setIsDepreciable(true);
        saved.setUsefulLife(60);

        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(saved));
        when(depreciationUseCase.createInitialDepreciation(any())).thenReturn(Mono.just(new pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse()));

        StepVerifier.create(assetService.create(request))
                .expectNextCount(1)
                .verifyComplete();

        verify(depreciationUseCase).createInitialDepreciation(argThat(a ->
                a.getIsDepreciable() && a.getUsefulLife() == 60));
    }

    @Test
    void create_WhenSbnCodeProvided_ShouldValidateAgainstCatalog() {
        AssetRequest request = new AssetRequest();
        request.setAssetCode("SBN-001");
        request.setSbnCode("51111001");
        request.setAcquisitionValue(new BigDecimal("1000.00"));

        when(sbnCatalogPersistencePort.findByCodigo("51111001")).thenReturn(Mono.just(new pe.edu.vallegrande.patrimonio_service.domain.model.SbnCatalog()));
        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(asset));

        StepVerifier.create(assetService.create(request))
                .expectNextCount(1)
                .verifyComplete();

        verify(sbnCatalogPersistencePort).findByCodigo("51111001");
    }

    @Test
    void create_WhenSbnCodeNotInCatalog_ShouldThrowError() {
        AssetRequest request = new AssetRequest();
        request.setAssetCode("SBN-002");
        request.setSbnCode("INVALID");
        request.setAcquisitionValue(new BigDecimal("1000.00"));

        when(sbnCatalogPersistencePort.findByCodigo("INVALID")).thenReturn(Mono.empty());
        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(new pe.edu.vallegrande.patrimonio_service.domain.model.Asset()));

        StepVerifier.create(assetService.create(request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void update_WhenDepreciationFieldsChanged_ShouldRecalculate() {
        AssetRequest updateRequest = new AssetRequest();
        updateRequest.setAssetCode("AST-001");
        updateRequest.setDescription("Updated Laptop");
        updateRequest.setAcquisitionValue(new BigDecimal("5000.00"));
        updateRequest.setIsDepreciable(true);
        updateRequest.setUsefulLife(120);

        Asset updated = new Asset();
        updated.setId(assetId);
        updated.setAssetCode("AST-001");
        updated.setAcquisitionValue(new BigDecimal("5000.00"));
        updated.setIsDepreciable(true);
        updated.setUsefulLife(120);

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(updated));
        when(depreciationUseCase.recalculateForAsset(assetId)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.update(assetId, updateRequest))
                .expectNextCount(1)
                .verifyComplete();

        verify(depreciationUseCase).recalculateForAsset(assetId);
    }

    @Test
    void getById_WhenAssetExists_ShouldReturnAsset() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.getById(assetId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getId().equals(assetId))
                .verifyComplete();
    }

    @Test
    void getById_WhenAssetDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.empty());

        // Act
        Mono<AssetResponse> result = assetService.getById(assetId);

        // Assert
        StepVerifier.create(result)
                .expectError(AssetNotFoundException.class)
                .verify();
    }

    @Test
    void changeStatus_WhenAssetExists_ShouldUpdateStatus() {
        // Arrange
        String nuevoEstado = "BA";
        CambioEstadoRequest cambioRequest = new CambioEstadoRequest();
        cambioRequest.setNuevoEstado(nuevoEstado);
        cambioRequest.setObservaciones("Baja por obsolescencia");

        Asset assetActualizado = new Asset();
        assetActualizado.setId(assetId);
        assetActualizado.setAssetStatus(nuevoEstado);

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(assetActualizado));

        // Act
        Mono<AssetResponse> result = assetService.changeStatus(assetId, cambioRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getAssetStatus().equals(nuevoEstado))
                .verifyComplete();

        verify(persistencePort).save(argThat(a -> a.getAssetStatus().equals(nuevoEstado)));
    }

    @Test
    void findByAssetCode_WhenExists_ShouldReturnAsset() {
        // Arrange
        String code = "AST-001";
        when(persistencePort.findByAssetCode(code)).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.findByAssetCode(code);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getAssetCode().equals(code))
                .verifyComplete();
    }

    @Test
    void createBatch_WhenValidRequests_ShouldReturnSavedAssets() {
        // Arrange
        AssetRequest req1 = new AssetRequest();
        req1.setAssetCode("BATCH-001");
        req1.setAcquisitionValue(new BigDecimal("100.00"));

        AssetRequest req2 = new AssetRequest();
        req2.setAssetCode("BATCH-002");
        req2.setAcquisitionValue(new BigDecimal("200.00"));

        Flux<AssetRequest> requests = Flux.just(req1, req2);

        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.empty());
        when(persistencePort.save(any(Asset.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act
        Flux<AssetResponse> result = assetService.createBatch(requests);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(res -> res.getAssetCode().equals("BATCH-001"))
                .expectNextMatches(res -> res.getAssetCode().equals("BATCH-002"))
                .verifyComplete();

        verify(persistencePort, times(2)).save(any(Asset.class));
    }

    @ParameterizedTest(name = "Prueba #{index}: Filtrando activos con estado ''{1}'' ({0})")
    @CsvSource({
            "AC, ACTIVO",
            "BA, DADO DE BAJA",
            "RE, EN REPARACIÓN",
            "IN, INACTIVO"
    })
    void findByStatus_DeberiaFiltrarPorEstadoEnviado(String codigo, String descripcion) {
        // LOG DE INICIO
        System.out.println("\n>>> [INI] Probando flujo para: " + descripcion + " (" + codigo + ")");
        // Arrange
        Asset activoFicticio = new Asset();
        activoFicticio.setAssetStatus(codigo);
        activoFicticio.setDescription("Activo de prueba: " + descripcion);

        when(persistencePort.findByAssetStatus(codigo)).thenReturn(Flux.just(activoFicticio));
        System.out.println(">>> [ARR] Mock configurado para responder con estado: " + codigo);
        // Act
        Flux<AssetResponse> resultado = assetService.findByStatus(codigo);
        System.out.println(">>> [ACT] Llamada al servicio findByStatus ejecutada.");
        // Assert
        StepVerifier.create(resultado)
                .assertNext(respuesta -> {
                    System.out.println(">>> [ASS] Verificando respuesta: " + respuesta.getAssetStatus());
                    assertEquals(codigo, respuesta.getAssetStatus(),
                            "El estado devuelto debería ser " + descripcion);
                })
                .verifyComplete();

        System.out.println(">>> [FIN] Caso " + descripcion + " completado con éxito.\n");
    }

    @Test
    void create_WhenAssetCodeAlreadyExists_ShouldThrowException() {
        // Arrange
        when(persistencePort.findByAssetCode(anyString())).thenReturn(Mono.just(asset));
        // Mock necesario porque switchIfEmpty evalúa su argumento
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(assetService.create(assetRequest))
                .expectError(pe.edu.vallegrande.patrimonio_service.domain.exception.AssetAlreadyExistsException.class)
                .verify();
    }

    @Test
    void getAll_ShouldReturnAllAssets() {
        // Arrange
        when(persistencePort.findAll()).thenReturn(Flux.just(asset));

        // Act & Assert
        StepVerifier.create(assetService.getAll())
                .expectNextMatches(res -> res.getAssetCode().equals(asset.getAssetCode()))
                .verifyComplete();
    }

    @Test
    void delete_WhenAssetExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.deleteById(assetId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(assetService.delete(assetId))
                .verifyComplete();

        verify(persistencePort).deleteById(assetId);
    }

    @Test
    void delete_WhenAssetDoesNotExist_ShouldThrowNotFound() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(assetService.delete(assetId))
                .expectError(AssetNotFoundException.class)
                .verify();
    }

    @Test
    void update_WhenAssetDoesNotExist_ShouldThrowNotFound() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(assetService.update(assetId, assetRequest))
                .expectError(AssetNotFoundException.class)
                .verify();
    }

    @Test
    void changeStatus_WhenAssetDoesNotExist_ShouldThrowNotFound() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(assetService.changeStatus(assetId, new CambioEstadoRequest()))
                .expectError(AssetNotFoundException.class)
                .verify();
    }

    @Test
    void findLastAssetCodeStartingWith_WhenExists_ShouldReturnCode() {
        String prefix = "AST-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("AST-005"));

        StepVerifier.create(assetService.findLastAssetCodeStartingWith(prefix))
                .expectNext("AST-005")
                .verifyComplete();
    }

    @Test
    void findLastAssetCodeStartingWith_WhenNotExists_ShouldReturnDefault() {
        String prefix = "AST-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.findLastAssetCodeStartingWith(prefix))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    void findNextSequence_ShouldReturnMockedResponse() {
        // Arrange
        String prefix = "CON-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("CON-010"));

        // Act & Assert
        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("011"))
                .verifyComplete();
    }

    @Test
    void findNextSequence_WhenLastCodeIsNull_ShouldReturn001() {
        String prefix = "NEW-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("001") && res.getLastCode() == null)
                .verifyComplete();
    }

    @Test
    void findNextSequence_WhenLastCodeStartsWithPrefix_ShouldExtractSuffix() {
        String prefix = "PRE-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("PRE-123"));

        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("124") && res.getLastCode().equals("PRE-123"))
                .verifyComplete();
    }

    @Test
    void findNextSequence_WhenLastCodeDifferentPrefix_ShouldExtractDigits() {
        String prefix = "XYZ-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("ABC-099"));

        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("100"))
                .verifyComplete();
    }

    @Test
    void findNextSequence_WhenSuffixHasLeadingZeros_ShouldPreserveWidth() {
        String prefix = "ZER-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("ZER-009"));

        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("010"))
                .verifyComplete();
    }

    @Test
    void findNextSequence_WhenSuffixNotNumeric_ShouldReturn001() {
        String prefix = "ABC-";
        when(persistencePort.findLastAssetCodeStartingWith(prefix)).thenReturn(Mono.just("ABC-XYZ"));

        StepVerifier.create(assetService.findNextSequence(prefix))
                .expectNextMatches(res -> res.getNextSeq().equals("001"))
                .verifyComplete();
    }

    @Test
    void update_WhenAssetExists_ShouldUpdateSuccessfully() {
        AssetRequest updateRequest = new AssetRequest();
        updateRequest.setAssetCode("AST-002");
        updateRequest.setDescription("Updated Laptop");
        updateRequest.setAcquisitionValue(new BigDecimal("4000.00"));

        Asset updatedAsset = new Asset();
        updatedAsset.setId(assetId);
        updatedAsset.setAssetCode("AST-002");
        updatedAsset.setDescription("Updated Laptop");

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(updatedAsset));

        StepVerifier.create(assetService.update(assetId, updateRequest))
                .expectNextMatches(res -> res.getAssetCode().equals("AST-002") && res.getDescription().equals("Updated Laptop"))
                .verifyComplete();
    }

    @Test
    void changeStatus_WhenModificadoPorIsNull_ShouldNotSetUpdatedBy() {
        CambioEstadoRequest cambioRequest = new CambioEstadoRequest();
        cambioRequest.setNuevoEstado("BA");
        cambioRequest.setObservaciones("Baja");
        cambioRequest.setModificadoPor(null);

        Asset assetActualizado = new Asset();
        assetActualizado.setId(assetId);
        assetActualizado.setAssetStatus("BA");

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(assetActualizado));

        StepVerifier.create(assetService.changeStatus(assetId, cambioRequest))
                .expectNextMatches(response -> response.getAssetStatus().equals("BA"))
                .verifyComplete();
    }

    @Test
    void changeStatus_WhenModificadoPorIsProvided_ShouldSetUpdatedBy() {
        UUID modificadoPor = UUID.randomUUID();
        CambioEstadoRequest cambioRequest = new CambioEstadoRequest();
        cambioRequest.setNuevoEstado("BA");
        cambioRequest.setObservaciones("Baja");
        cambioRequest.setModificadoPor(modificadoPor);

        Asset assetActualizado = new Asset();
        assetActualizado.setId(assetId);
        assetActualizado.setAssetStatus("BA");
        assetActualizado.setUpdatedBy(modificadoPor);

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(argThat(a -> a.getUpdatedBy() != null && a.getUpdatedBy().equals(modificadoPor)))).thenReturn(Mono.just(assetActualizado));

        StepVerifier.create(assetService.changeStatus(assetId, cambioRequest))
                .expectNextMatches(response -> response.getAssetStatus().equals("BA"))
                .verifyComplete();
    }

    @Test
    void findByAssetCode_WhenNotFound_ShouldThrowException() {
        String code = "NON-EXISTENT";
        when(persistencePort.findByAssetCode(code)).thenReturn(Mono.empty());

        StepVerifier.create(assetService.findByAssetCode(code))
                .expectError(AssetNotFoundException.class)
                .verify();
    }
}

package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetAlreadyExistsException;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.exception.InvalidDisposalStateException;

import java.util.Map;

@DisplayName("GlobalExceptionHandler - Manejador global de excepciones")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAssetAlreadyExists_ShouldReturnConflict() {
        AssetAlreadyExistsException ex = new AssetAlreadyExistsException("Asset already exists");
        
        ResponseEntity<Map<String, String>> response = handler.handleAssetAlreadyExists(ex);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Asset already exists", response.getBody().get("message"));
    }

    @Test
    void handleNotFound_ShouldReturnNotFound() {
        AssetNotFoundException ex = new AssetNotFoundException("Asset not found");
        
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Asset not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleInvalidDisposalState: Retorna 400 BAD_REQUEST cuando el estado de disposición es inválido")
    void handleInvalidDisposalState_ShouldReturnBadRequest() {
        InvalidDisposalStateException ex = new InvalidDisposalStateException("Invalid state transition");
        
        ResponseEntity<Map<String, String>> response = handler.handleInvalidDisposalState(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid state transition", response.getBody().get("message"));
    }
}

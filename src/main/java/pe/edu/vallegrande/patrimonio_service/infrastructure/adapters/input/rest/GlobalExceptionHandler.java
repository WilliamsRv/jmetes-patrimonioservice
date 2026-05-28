package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pe.edu.vallegrande.patrimonio_service.domain.exception.*;

import java.util.Collections;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(AssetAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAssetAlreadyExists(AssetAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler({AssetNotFoundException.class, AssetDisposalNotFoundException.class, AssetDisposalDetailNotFoundException.class, DepreciationNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(MESSAGE_KEY, ex.getMessage()));
    }

    @ExceptionHandler(InvalidDisposalStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDisposalState(InvalidDisposalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(MESSAGE_KEY, ex.getMessage()));
    }
}

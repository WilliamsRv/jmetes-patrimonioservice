package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SBNValidationResponse {
    private boolean exists;
    private String assetCode;
    private String description;
}

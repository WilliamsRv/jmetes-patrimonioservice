package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SbnCatalogResponse {
    private String codigo;
    private String descripcion;
    private String grupo;
    private String clase;
    private BigDecimal tasaDepreciacionAnual;
    private Integer vidaUtilMeses;
    private Boolean esDepreciable;
    private Boolean requiereSerieMarcaModelo;
}

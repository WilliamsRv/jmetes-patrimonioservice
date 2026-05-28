package pe.edu.vallegrande.patrimonio_service.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("sbn_catalog")
public class SbnCatalog {
    @Id
    private UUID id;
    private String codigo;
    private String descripcion;
    private String grupo;
    private String clase;
    private BigDecimal tasaDepreciacionAnual;
    private Integer vidaUtilMeses;
    private Boolean esDepreciable;
    private Boolean requiereSerieMarcaModelo;
    private LocalDateTime createdAt;
}

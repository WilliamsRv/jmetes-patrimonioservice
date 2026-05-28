package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;

@Data
public class NextSeqResponse {
    private String nextSeq;
    private String lastCode;
    private String fullAssetCode;
}

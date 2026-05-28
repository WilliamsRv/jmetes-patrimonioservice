package pe.edu.vallegrande.patrimonio_service.functional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetDisposalFunctionalTest {

    private static final String API = "http://localhost:5003";
    private static String createdDisposalId;
    private static final String FILE_NUMBER = "FUNC-BAJA-" + System.currentTimeMillis();

    @Test
    @Order(1)
    @DisplayName("RF-006: Crear solicitud de baja de activo")
    void testCreateDisposal() {
        String body = """
            {"fileNumber":"%s","disposalType":"ADMINISTRATIVE","disposalReason":"OBSOLESCENCE","reasonDescription":"Activo obsoleto","technicalReportAuthorId":"00000000-0000-0000-0000-000000000001","requestedBy":"00000000-0000-0000-0000-000000000001"}
            """.formatted(FILE_NUMBER);

        Response response = given()
            .contentType(ContentType.JSON)
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
            .body(body)
        .when()
            .post(API + "/api/v1/asset-disposals")
        .then()
            .statusCode(200)
            .body("fileNumber", equalTo(FILE_NUMBER))
            .body("fileStatus", equalTo("INITIATED"))
            .extract().response();

        createdDisposalId = response.jsonPath().getString("id");
        Assertions.assertNotNull(createdDisposalId, "El ID de la baja no debe ser nulo");
        System.out.println("[OK] Baja creada con ID: " + createdDisposalId);
    }

    @Test
    @Order(2)
    @DisplayName("RF-007: Consultar baja por numero de expediente")
    void testGetDisposalByFileNumber() {
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .get(API + "/api/v1/asset-disposals/file-number/{fileNumber}", FILE_NUMBER)
        .then()
            .statusCode(200)
            .body("fileNumber", equalTo(FILE_NUMBER));
    }

    @Test
    @Order(3)
    @DisplayName("RF-008: Consultar bajas por estado INITIATED")
    void testGetDisposalsByStatus() {
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .get(API + "/api/v1/asset-disposals/status/INITIATED")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    @Order(4)
    @DisplayName("RF-009: Asignar comite a una solicitud de baja")
    void testAssignCommittee() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
            .body("{\"committeeMembers\":[\"00000000-0000-0000-0000-000000000003\"],\"committeePresidentId\":\"00000000-0000-0000-0000-000000000002\"}")
        .when()
            .put(API + "/api/v1/asset-disposals/{id}/assign-committee", createdDisposalId)
        .then()
            .statusCode(200);
    }

    @Test
    @Order(5)
    @DisplayName("RF-010: Listar todas las solicitudes de baja")
    void testListAllDisposals() {
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .get(API + "/api/v1/asset-disposals")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}

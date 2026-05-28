package pe.edu.vallegrande.patrimonio_service.functional;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetFunctionalTest {

    private static final String API = "http://localhost:5003";
    private static String createdAssetId;
    private static final String ASSET_CODE = "FUNC-" + System.currentTimeMillis();

    @Test
    @Order(1)
    @DisplayName("RF-001: Registrar un nuevo activo patrimonial")
    void testCreateAsset() {
        String body = """
            {"assetCode":"%s","description":"Laptop test","acquisitionDate":"2025-01-15","acquisitionValue":4500,"assetStatus":"AVAILABLE","createdBy":"00000000-0000-0000-0000-000000000001"}
            """.formatted(ASSET_CODE);

        Response response = given()
            .contentType(ContentType.JSON)
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
            .body(body)
        .when()
            .post(API + "/api/v1/assets")
        .then()
            .statusCode(201)
            .body("assetCode", equalTo(ASSET_CODE))
            .extract().response();

        createdAssetId = response.jsonPath().getString("id");
        Assertions.assertNotNull(createdAssetId, "El ID del activo no debe ser nulo");
        System.out.println("[OK] Activo creado con ID: " + createdAssetId);
    }

    @Test
    @Order(2)
    @DisplayName("RF-002: Consultar activo por ID")
    void testGetAssetById() {
        String id = createdAssetId;
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .get(API + "/api/v1/assets/{id}", id)
        .then()
            .statusCode(200)
            .body("assetCode", equalTo(ASSET_CODE));
    }

    @Test
    @Order(3)
    @DisplayName("RF-003: Listar todos los activos")
    void testListAllAssets() {
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .get(API + "/api/v1/assets")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    @Order(4)
    @DisplayName("RF-004: Actualizar un activo existente")
    void testUpdateAsset() {
        String body = """
            {"assetCode":"%s","description":"Laptop test ACTUALIZADA","acquisitionDate":"2025-01-15","acquisitionValue":5000,"assetStatus":"AVAILABLE","createdBy":"00000000-0000-0000-0000-000000000001"}
            """.formatted(ASSET_CODE);

        given()
            .contentType(ContentType.JSON)
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
            .body(body)
        .when()
            .put(API + "/api/v1/assets/{id}", createdAssetId)
        .then()
            .statusCode(200)
            .body("description", containsString("ACTUALIZADA"));
    }

    @Test
    @Order(5)
    @DisplayName("RF-005: Cambiar estado de un activo")
    void testChangeAssetStatus() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
            .body("{\"newStatus\":\"MAINTENANCE\"}")
        .when()
            .patch(API + "/api/v1/assets/{id}/status", createdAssetId)
        .then()
            .statusCode(200)
            .body("assetStatus", equalTo("MAINTENANCE"));
    }

    @Test
    @Order(6)
    @DisplayName("RF-005b: Eliminar un activo")
    void testDeleteAsset() {
        given()
            .header("X-Municipal-Code", "550e8400-e29b-41d4-a716-446655440000")
            .header("X-User-Id", "550e8400-e29b-41d4-a716-446655440002")
            .header("X-Roles", "TENANT_ADMIN")
        .when()
            .delete(API + "/api/v1/assets/{id}", createdAssetId)
        .then()
            .statusCode(204);
    }
}

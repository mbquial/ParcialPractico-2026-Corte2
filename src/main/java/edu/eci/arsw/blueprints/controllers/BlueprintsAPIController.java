package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @Operation(summary = "Obtener todos los planos")
    @ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<ApiResponseQ<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(new ApiResponseQ<>(200, "Consulta exitosa", services.getAllBlueprints()));
    }

    // GET /blueprints/{author}
    @Operation(summary = "Obtener todos los planos por autor")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Consulta exitosa"), @ApiResponse(responseCode = "404", description = "Recurso no existente o no se enceuntra")})
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponseQ<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok( new ApiResponseQ<>(200, "Consulta exitosa", services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseQ<>(404, e.getMessage(), null));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Obtener planos por nombre y autor")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Consulta exitosa"), @ApiResponse(responseCode = "404", description = "Recurso no existente o no se enceuntra")})
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(new ApiResponseQ<>(200, "Consulta exitosa", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseQ<>(404, e.getMessage(), null));
        }
    }

    // POST /blueprints
    @Operation(summary = "Crear plano")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Creación exitosa"), @ApiResponse(responseCode = "400", description = "Datos inválidos"),
    })
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseQ<>(201, "Creación exitosa", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponseQ<>(401, "DAtos inválidos", null));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Agrega un punto")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "Punto agregado exitosamente"), @ApiResponse(responseCode = "404", description = "Recurso no existente o no se enceuntra")})
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseQ<>(202, "Punto agregado exitosamente", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseQ<>(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}

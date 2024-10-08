package ika;

import com.fasterxml.jackson.databind.ObjectMapper;
import ika.controllers.medication.MedicationRequest;
import ika.entities.Medication;
import ika.repositories.MedicationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usar perfil de teste
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicationRepository medicationRepository;

    // Definir o contêiner PostgreSQL como estático e anotá-lo com @Container para Testcontainers gerenciar
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.1")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Configurar propriedades dinâmicas para o Spring usar o PostgreSQL do Testcontainers
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void setup() {
        postgres.start();
    }

    @Test
    void testCreateMedicationSuccess() throws Exception {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setName("Paracetamol");
        medicationRequest.setDosage(500);
        medicationRequest.setActiveIngredientId(UUID.randomUUID());
        medicationRequest.setCategoryId(UUID.randomUUID());
        medicationRequest.setBand(3);
        medicationRequest.setTimeBetween(6);
        medicationRequest.setMaxTime(12);

        mockMvc.perform(post("/v1/medications/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Medication created successfully"));
    }

    @Test
    void testGetMedicationByIdSuccess() throws Exception {
        // Create and save a medication entity to the database first
        Medication medication = new Medication();
        medication.setId(UUID.randomUUID());
        medication.setName("Ibuprofen");
        medication.setDosage(400);
        medication.setBand(3);
        medication.setTimeBetween(8);
        medication.setMaxTime(24);
        medication = medicationRepository.save(medication);

        // Perform the GET request to retrieve the medication by ID
        mockMvc.perform(get("/v1/medications/" + medication.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ibuprofen"))
                .andExpect(jsonPath("$.dosage").value(400));
    }

    @Test
    void testGetMedicationByIdNotFound() throws Exception {
        // Create a random UUID for a non-existent medication
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/v1/medications/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

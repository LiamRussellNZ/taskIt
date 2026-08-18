package nz.taskit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import nz.taskit.domain.UserRole;
import nz.taskit.repository.AppUserRepository;
import nz.taskit.repository.TaskRepository;
import nz.taskit.web.dto.UserCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskApiIntegrationTest {

    private static final String USER_HEADER = "X-User-Id";
    private static final String TASK_JSON = """
            {"title":"Pick up groceries","description":"Collect a small order","category":"Errands","location":"Wellington","remote":false}
            """;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("taskit")
            .withUsername("taskit")
            .withPassword("taskit");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TaskRepository tasks;

    @Autowired
    AppUserRepository users;

    @BeforeEach
    void clearData() {
        tasks.deleteAll();
        users.deleteAll();
    }

    @Test
    void askerCanCreateAndOpenBoardCanFilterByCategory() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);

        mvc.perform(post("/api/tasks").header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON).content(TASK_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.asker.id").value(asker));

        mvc.perform(get("/api/tasks").param("category", "errands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pick up groceries"));
    }

    @Test
    void roleAndOwnershipRestrictionsAreApplied() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long outsider = createUser("Owen", "owen@example.test", UserRole.ASKER);
        long taskId = createTask(asker);

        mvc.perform(post("/api/tasks").header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON).content(TASK_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/tasks/{id}", taskId).header(USER_HEADER, outsider)
                        .contentType(MediaType.APPLICATION_JSON).content(TASK_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, asker))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLAIMED"))
                .andExpect(jsonPath("$.assignedDoer.id").value(doer));

        mvc.perform(post("/api/tasks/{id}/complete", taskId).header(USER_HEADER, outsider))
                .andExpect(status().isForbidden());
    }

    @Test
    void validTransitionsAllowAssigneeCompletionAndPreventInvalidTransitions() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long taskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/tasks/{id}/complete", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/tasks/{id}/cancel", taskId).header(USER_HEADER, asker))
                .andExpect(status().isConflict());
    }

    @Test
    void doerCompletionNotifiesAskerAndCanBeReviewedOnce() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long taskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/tasks/{id}/complete", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mvc.perform(get("/api/users/{id}/notifications", asker).header(USER_HEADER, asker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TASK_COMPLETED"))
                .andExpect(jsonPath("$[0].actor.id").value(doer))
                .andExpect(jsonPath("$[0].taskId").value(taskId));

        mvc.perform(post("/api/tasks/{id}/completion-review", taskId).header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"review\":\"Excellent work\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks/{id}/completion-review", taskId).header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"review\":\"Excellent work\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionReview.rating").value(5))
                .andExpect(jsonPath("$.completionReview.doer.id").value(doer))
                .andExpect(jsonPath("$.completionReview.review").value("Excellent work"));
        mvc.perform(post("/api/tasks/{id}/completion-review", taskId).header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"review\":\"Another review\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void askerCanRequestAndAssignedDoerCanRespondToAStatusUpdate() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long outsider = createUser("Owen", "owen@example.test", UserRole.DOER);
        long taskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());

        String requested = mvc.perform(post("/api/tasks/{id}/status-updates", taskId).header(USER_HEADER, asker))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusUpdates[0].response").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long statusUpdateId = objectMapper.readTree(requested).at("/statusUpdates/0/id").asLong();

        mvc.perform(post("/api/tasks/{id}/status-updates/{statusUpdateId}/respond", taskId, statusUpdateId)
                        .header(USER_HEADER, outsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"response\":\"I am on the way\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/tasks/{id}/status-updates/{statusUpdateId}/respond", taskId, statusUpdateId)
                        .header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"response\":\"I am on the way\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusUpdates[0].response").value("I am on the way"))
                .andExpect(jsonPath("$.statusUpdates[0].respondedAt").exists());
    }

    @Test
    void assignedDoerCanDropOnceAndAskerCanReviewTheDrop() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long replacementDoer = createUser("Riley", "riley@example.test", UserRole.DOER);
        long taskId = createTask(asker);

        String dropped = mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/tasks/{id}/drop", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.assignedDoer").doesNotExist())
                .andExpect(jsonPath("$.drops[0].doer.id").value(doer));

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, replacementDoer))
                .andExpect(status().isOk());

        long dropId = objectMapper.readTree(mvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).at("/drops/0/id").asLong();
        mvc.perform(post("/api/tasks/{id}/drops/{dropId}/review", taskId, dropId)
                        .header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":1,\"review\":\"Did not follow through\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks/{id}/drops/{dropId}/review", taskId, dropId)
                        .header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":1,\"review\":\"Did not follow through\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drops[0].rating").value(1))
                .andExpect(jsonPath("$.drops[0].review").value("Did not follow through"));
    }

    @Test
    void assignedDoerCanRequestAssistanceAndOnlyOneOtherDoerCanOfferIt() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long assignedDoer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long helper = createUser("Harper", "harper@example.test", UserRole.DOER);
        long otherHelper = createUser("Owen", "owen@example.test", UserRole.DOER);
        long taskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, assignedDoer))
                .andExpect(status().isOk());

        mvc.perform(post("/api/tasks/{id}/assistance-requests", taskId).header(USER_HEADER, helper))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/tasks/{id}/assistance-requests", taskId).header(USER_HEADER, assignedDoer))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assistanceRequest.requestingDoer.id").value(assignedDoer));

        mvc.perform(get("/api/tasks").param("view", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].status").value("CLAIMED"))
                .andExpect(jsonPath("$[0].assistanceRequest.helper").isEmpty());

        mvc.perform(post("/api/tasks/{id}/assistance-requests/offer", taskId).header(USER_HEADER, helper))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistanceRequest.helper.id").value(helper));

        mvc.perform(post("/api/tasks/{id}/assistance-requests/offer", taskId).header(USER_HEADER, otherHelper))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/tasks/{id}/complete", taskId).header(USER_HEADER, helper))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks/{id}/drop", taskId).header(USER_HEADER, helper))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/users/{id}/notifications", asker).header(USER_HEADER, asker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("ASSISTANCE_REQUEST"))
                .andExpect(jsonPath("$[0].taskId").value(taskId))
                .andExpect(jsonPath("$[0].actor.id").value(assignedDoer));
        mvc.perform(get("/api/users/{id}/notifications", asker).header(USER_HEADER, helper))
                .andExpect(status().isForbidden());
    }

    @Test
    void doerCanAskOpenTaskQuestionAndOnlyAskerCanAnswerOnce() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long outsider = createUser("Owen", "owen@example.test", UserRole.ASKER);
        long taskId = createTask(asker);

        String questioned = mvc.perform(post("/api/tasks/{id}/questions", taskId).header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Is parking available?\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questions[0].askingDoer.id").value(doer))
                .andExpect(jsonPath("$.questions[0].answer").isEmpty())
                .andReturn().getResponse().getContentAsString();
        long questionId = objectMapper.readTree(questioned).at("/questions/0/id").asLong();

        mvc.perform(get("/api/users/{id}/notifications", asker).header(USER_HEADER, asker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TASK_QUESTION"))
                .andExpect(jsonPath("$[0].actor.id").value(doer));

        mvc.perform(post("/api/tasks/{id}/questions/{questionId}/answer", taskId, questionId)
                        .header(USER_HEADER, outsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Yes\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks/{id}/questions/{questionId}/answer", taskId, questionId)
                        .header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Yes, on the street.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].answer").value("Yes, on the street."));
        mvc.perform(post("/api/tasks/{id}/questions/{questionId}/answer", taskId, questionId)
                        .header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"Another answer\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/tasks/{id}/claim", taskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/tasks/{id}/questions", taskId).header(USER_HEADER, doer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Can I still ask?\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void profileIncludesRatingsReviewsAndTaskHistory() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        long doer = createUser("Dane", "dane@example.test", UserRole.DOER);
        long reviewedTaskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", reviewedTaskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/tasks/{id}/complete", reviewedTaskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/tasks/{id}/completion-review", reviewedTaskId).header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"review\":\"Clear communication\"}"))
                .andExpect(status().isOk());

        long droppedTaskId = createTask(asker);

        mvc.perform(post("/api/tasks/{id}/claim", droppedTaskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());
        String dropped = mvc.perform(post("/api/tasks/{id}/drop", droppedTaskId).header(USER_HEADER, doer))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long dropId = objectMapper.readTree(dropped).at("/drops/0/id").asLong();
        mvc.perform(post("/api/tasks/{id}/drops/{dropId}/review", droppedTaskId, dropId)
                        .header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"review\":\"Clear communication\"}"))
                .andExpect(status().isOk());

        long currentTaskId = createTask(asker);
        mvc.perform(post("/api/tasks/{id}/claim", currentTaskId).header(USER_HEADER, doer))
                .andExpect(status().isOk());

        mvc.perform(get("/api/users/{id}/profile", doer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageReceivedRating").value(4.0))
                .andExpect(jsonPath("$.reviews[0].review").value("Clear communication"))
                .andExpect(jsonPath("$.reviews[0].task.id").value(reviewedTaskId))
                .andExpect(jsonPath("$.priorAssignments[0].task.id").value(droppedTaskId))
                .andExpect(jsonPath("$.currentAssignments[0].id").value(currentTaskId));
        mvc.perform(get("/api/users/{id}/profile", asker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedTasks.length()").value(3));
    }

    @Test
    void rejectsInvalidTaskPayloadAndMissingIdentity() throws Exception {
        long asker = createUser("Asha", "asha@example.test", UserRole.ASKER);
        String invalid = """
                {"title":"","description":"","category":"","location":"","remote":false}
                """;

        mvc.perform(post("/api/tasks").header(USER_HEADER, asker)
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("title")));

        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(TASK_JSON))
                .andExpect(status().isBadRequest());
    }

    private long createUser(String name, String email, UserRole... roles) throws Exception {
        String response = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest(name, email, Set.of(roles)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createTask(long askerId) throws Exception {
        String response = mvc.perform(post("/api/tasks").header(USER_HEADER, askerId)
                        .contentType(MediaType.APPLICATION_JSON).content(TASK_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}

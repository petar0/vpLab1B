package mk.ukim.finki.wp.lab;

import mk.ukim.finki.wp.lab.boostrap.DataHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

// proverki so vistinski HTTP baranja do servletite i HTML stranicite
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamWebIntegrationTests {

    @Autowired private Environment environment;
    @Autowired private DataHolder dataHolder;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER).build();
    private String baseUrl;

    @BeforeEach
    void resetData() {
        dataHolder.init();
        baseUrl = "http://localhost:" + environment.getProperty("local.server.port");
    }

    @Test
    void teamPageListsFiveRealTeamsAndTheirPlayerCounts() throws Exception {
        HttpResponse<String> response = get("/teams");
        assertEquals(200, response.statusCode());
        assertEquals(5, radioCount(response.body()));
        assertTrue(response.body().contains("Name: Vardar, City: Skopje, Players: 0"));
        assertTrue(response.body().contains("Name: Tikves, City: Kavadarci, Players: 0"));
        assertFalse(response.body().contains("th:each="));
    }

    @Test
    void playerPageListsAllPlayersAndPassesSelectedTeamInHiddenField() throws Exception {
        HttpResponse<String> response = get("/players?teamId=2");
        assertEquals(200, response.statusCode());
        assertEquals(10, radioCount(response.body()));
        assertTrue(response.body().contains("Marko Markovski (#9, Forward)"));
        assertTrue(response.body().contains("Petar Petrovski (#9, Midfielder)"));
        assertTrue(response.body().contains("name=\"teamId\" value=\"2\""));
        assertTrue(response.body().contains("action=\"/teamDetails\" method=\"POST\""));
        assertTrue(response.body().contains("Pelister"));
        assertTrue(response.body().contains("Bitola"));
        assertFalse(response.body().contains("<p class=\"error\""));
    }

    @Test
    void missingMalformedOrUnknownTeamRedirectsToTeams() throws Exception {
        for (String page : new String[]{"/players", "/teamDetails"}) {
            for (String query : new String[]{"", "?teamId=", "?teamId=abc", "?teamId=999"}) {
                HttpResponse<String> response = get(page + query);
                assertEquals(302, response.statusCode(), page + query);
                assertEquals("/teams", location(response));
            }
        }
    }

    @Test
    void emptyTeamDetailsShowsEmptyMessageAndLinksForTheSelectedTeam() throws Exception {
        HttpResponse<String> response = get("/teamDetails?teamId=2");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Team: Pelister"));
        assertTrue(response.body().contains("City: Bitola"));
        assertTrue(response.body().contains("No players yet"));
        assertTrue(response.body().contains("href=\"/players?teamId=2\""));
        assertTrue(response.body().contains("href=\"/teams\""));
    }

    @Test
    void successfulPostRedirectsToGetAndRefreshDoesNotAddAgain() throws Exception {
        HttpResponse<String> response = post("1", "p1");
        assertEquals(302, response.statusCode());
        assertEquals("/teamDetails?teamId=1", location(response));

        for (int refresh = 0; refresh < 2; refresh++) {
            HttpResponse<String> details = get(location(response));
            assertEquals(200, details.statusCode());
            assertTrue(details.body().contains("Team: Vardar"));
            assertTrue(details.body().contains("Marko Markovski (#9, Forward)"));
            assertFalse(details.body().contains("No players yet"));
        }

        assertEquals(1, DataHolder.teams.getFirst().getPlayers().size());
        assertTrue(get("/teams").body().contains("Name: Vardar, City: Skopje, Players: 1"));
        // listPlayers() i ponatamu gi prikazuva site igraci spored uslovot
        assertEquals(10, radioCount(get("/players?teamId=2").body()));
    }

    @Test
    void playerAlreadyInAnyTeamRedirectsWithEncodedErrorAndDoesNotMove() throws Exception {
        post("1", "p1");
        HttpResponse<String> response = post("2", "p1");
        String message = "Player already plays for Vardar";

        assertEquals(302, response.statusCode());
        assertEquals("/players?teamId=2&errorMessage=" + encode(message), location(response));
        String page = get(location(response)).body();
        assertTrue(page.contains("<p class=\"error\">" + message + "</p>"));
        assertEquals(1, DataHolder.teams.getFirst().getPlayers().size());
        assertTrue(DataHolder.teams.get(1).getPlayers().isEmpty());
    }

    @Test
    void jerseyConflictRedirectsWithExactMessageAndDoesNotAddPlayer() throws Exception {
        post("1", "p1");
        HttpResponse<String> response = post("1", "p2");
        String message = "Jersey number 9 is already taken in this team";

        assertEquals(302, response.statusCode());
        assertEquals("/players?teamId=1&errorMessage=" + encode(message), location(response));
        assertTrue(get(location(response)).body().contains(message));
        assertEquals(1, DataHolder.teams.getFirst().getPlayers().size());
        // istiot broj e dozvolen vo drug tim
        assertEquals("/teamDetails?teamId=2", location(post("2", "p2")));
    }

    @Test
    void invalidPostDoesNotSaveAndErrorTextIsEscaped() throws Exception {
        for (String teamId : new String[]{null, "abc", "999"}) {
            HttpResponse<String> response = post(teamId, "p1");
            assertEquals(302, response.statusCode());
            assertTrue(location(response).startsWith("/players?teamId="));
        }
        for (String playerId : new String[]{null, "missing"}) {
            assertEquals("/players?teamId=1&errorMessage=" + encode("Player not found"),
                    location(post("1", playerId)));
        }
        assertTrue(DataHolder.teams.stream().allMatch(team -> team.getPlayers().isEmpty()));

        String error = "Грешка <script>alert(1)</script> & test";
        String page = get("/players?teamId=1&errorMessage=" + encode(error)).body();
        assertTrue(page.contains("Грешка &lt;script&gt;alert(1)&lt;/script&gt; &amp; test"));
        assertFalse(page.contains("<script>"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> post(String teamId, String playerId) throws Exception {
        String body = "teamId=" + encode(teamId == null ? "" : teamId);
        if (playerId != null) {
            body += "&playerId=" + encode(playerId);
        }
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/teamDetails"))
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private String location(HttpResponse<String> response) {
        // Tomcat moze da vrati celosen URL; ja proveruvame istata aplikacija i patkata
        URI target = URI.create(baseUrl).resolve(response.headers().firstValue("Location").orElseThrow());
        assertEquals(URI.create(baseUrl).getAuthority(), target.getAuthority());
        return target.getRawPath() + (target.getRawQuery() == null ? "" : "?" + target.getRawQuery());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private long radioCount(String html) {
        return Pattern.compile("<input\\b[^>]*type=\"radio\"").matcher(html).results().count();
    }
}

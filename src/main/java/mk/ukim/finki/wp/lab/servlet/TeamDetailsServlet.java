package mk.ukim.finki.wp.lab.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.wp.lab.model.Team;
import mk.ukim.finki.wp.lab.service.TeamService;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/teamDetails")
public class TeamDetailsServlet extends HttpServlet {

    private final TeamService teamService;
    private final SpringTemplateEngine springTemplateEngine;

    public TeamDetailsServlet(TeamService teamService,
                              SpringTemplateEngine springTemplateEngine) {
        this.teamService = teamService;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String teamId = req.getParameter("teamId");
        String playerId = req.getParameter("playerId");
        Team team;

        try {
            // servisot gi proveruva pravilata i go dodava igracot vo timot
            team = teamService.addPlayerToTeam(Long.parseLong(teamId), playerId);
        } catch (IllegalArgumentException exception) {
            // ja enkodirame porakata za bezbedno da ja ispratime vo URL
            String message = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
            String encodedTeamId = URLEncoder.encode(teamId == null ? "" : teamId,
                    StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/players?teamId=" + encodedTeamId
                    + "&errorMessage=" + message);
            return;
        }

        // POST-Redirect-GET: osvezuvanje na detalite nema povtorno da dodade igrac
        resp.sendRedirect(req.getContextPath() + "/teamDetails?teamId=" + team.getId());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Team team;

        try {
            // ako id-to nedostasuva, ne e broj ili timot ne postoi, se vrakjame na listata
            team = teamService.findById(Long.parseLong(req.getParameter("teamId")));
        } catch (IllegalArgumentException exception) {
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        WebContext context = new WebContext(
                JakartaServletWebApplication
                        .buildApplication(getServletContext())
                        .buildExchange(req, resp)
        );

        context.setVariable("team", team);
        springTemplateEngine.process("teamDetails.html", context, resp.getWriter());
    }
}

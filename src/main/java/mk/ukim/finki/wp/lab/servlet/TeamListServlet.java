package mk.ukim.finki.wp.lab.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.wp.lab.service.TeamService;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/teams")
public class TeamListServlet extends HttpServlet {

    private final TeamService teamService;
    private final SpringTemplateEngine springTemplateEngine;

    public TeamListServlet(TeamService teamService,
                           SpringTemplateEngine springTemplateEngine) {
        this.teamService = teamService;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");

        WebContext context = new WebContext(
                JakartaServletWebApplication
                        .buildApplication(getServletContext())
                        .buildExchange(req, resp)
        );

        // gi zemame site timovi od servisot i gi predavame na HTML
        context.setVariable("teams", teamService.listTeams());
        springTemplateEngine.process("listTeams.html", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long teamId = Long.parseLong(req.getParameter("teamId"));
            teamService.deleteById(teamId);
        } catch (IllegalArgumentException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid team");
            return;
        }

        // po brisenjeto se vrakjame na listata preku novo GET baranje
        resp.sendRedirect(req.getContextPath() + "/teams");
    }
}

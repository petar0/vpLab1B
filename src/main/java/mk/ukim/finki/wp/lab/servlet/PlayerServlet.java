package mk.ukim.finki.wp.lab.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mk.ukim.finki.wp.lab.model.Team;
import mk.ukim.finki.wp.lab.service.PlayerService;
import mk.ukim.finki.wp.lab.service.TeamService;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/players")
public class PlayerServlet extends HttpServlet {
    private final PlayerService playerService;
    private final TeamService teamService;
    private final SpringTemplateEngine springTemplateEngine;

    public PlayerServlet(PlayerService playerService, TeamService teamService, SpringTemplateEngine springTemplateEngine) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.springTemplateEngine = springTemplateEngine;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException{
        String teamId= req.getParameter("teamId");//go cite parametarot teamid
        if (teamId == null) {// ako nedostasuva go redirectira na timovi
            resp.sendRedirect(req.getContextPath()+"/teams");
            return;
        }
        Team team;
        try {
            // go pretvorame id-to vo broj i go barame timot
            team = teamService.findById(Long.parseLong(teamId));
        } catch (IllegalArgumentException exception) {
            // ako timot ne postoi ili id-to ne e broj
            resp.sendRedirect(req.getContextPath() + "/teams");
            return;
        }
        resp.setContentType("text/html;charset=UTF-8");
        WebContext context = new WebContext(
                JakartaServletWebApplication
                        .buildApplication(getServletContext())
                        .buildExchange(req, resp)
        );

        context.setVariable("players", playerService.listPlayers());
        context.setVariable("team", team);
        context.setVariable("errorMessage", req.getParameter("errorMessage"));

        // go prikazuvame HTML so igracite i izbraniot tim
        springTemplateEngine.process("playersList.html", context, resp.getWriter());
    }
}

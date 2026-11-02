package mk.ukim.finki.wp.lab.service.impl;

import mk.ukim.finki.wp.lab.boostrap.DataHolder;
import mk.ukim.finki.wp.lab.model.Player;
import mk.ukim.finki.wp.lab.model.Team;
import mk.ukim.finki.wp.lab.repository.PlayerRepository;
import mk.ukim.finki.wp.lab.repository.TeamRepository;
import mk.ukim.finki.wp.lab.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Team> listTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() ->new IllegalArgumentException("Team not found"));
    }

    @Override
    public void deleteById(Long id) {
        // prvo proveruvame dali timot postoi, pa go briseme
        findById(id);
        teamRepository.deleteById(id);
    }

    @Override
    public Team addPlayerToTeam(Long teamId, String playerId) {
        // 1. Gi naogjame timot i igracot
        Team team = findById(teamId);

        Player player = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Player not found"));
        // 2. Proveruvame dali igracot vekje e vo nekoj tim
        for (Team t : teamRepository.findAll()) {
            for (Player p : t.getPlayers()) {

                if (p.getPlayerId().equals(playerId)) {
                    throw new IllegalArgumentException(
                            "Player already plays for " + t.getName());
                }
            }
        }

        // 3. Proveruvame dali brojot na dres e zafaten vo izbraniot tim
        for (Player p : team.getPlayers()) {

            if (p.getJerseyNumber() == player.getJerseyNumber()) {
                throw new IllegalArgumentException(
                        "Jersey number " + player.getJerseyNumber()
                                + " is already taken in this team");
            }
        }

        // 4. Go dodavame igracot, go zacuvuvame timot i go vrakjame
        team.getPlayers().add(player);
        return teamRepository.save(team);
    }
}

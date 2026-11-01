package mk.ukim.finki.wp.lab.boostrap;

import jakarta.annotation.PostConstruct;
import mk.ukim.finki.wp.lab.model.Player;
import mk.ukim.finki.wp.lab.model.Team;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class DataHolder {
    public static List<Team> teams = null;
    public static List<Player> players = null;

    @PostConstruct
    public void init(){
        teams = new ArrayList<Team>(); //sozdavame lista so 5 tima

        teams.add(new Team(1L, "Vardar", "Skopje", new ArrayList<>()));
        teams.add(new Team(2L, "Pelister", "Bitola", new ArrayList<>()));
        teams.add(new Team(3L, "Rabotnicki", "Skopje", new ArrayList<>()));
        teams.add(new Team(4L, "Sileks", "Kratovo", new ArrayList<>()));
        teams.add(new Team(5L, "Tikves", "Kavadarci", new ArrayList<>()));

        players=new ArrayList<Player>(); //sozdadovme lista so 10 igraci
        players.add(new Player("p1", "Marko", "Markovski", "Forward", 9));
        players.add(new Player("p2", "Petar", "Petrovski", "Midfielder", 9));
        players.add(new Player("p3", "Stefan", "Stefanovski", "Goalkeeper", 1));
        players.add(new Player("p4", "Ivan", "Ivanovski", "Defender", 4));
        players.add(new Player("p5", "Nikola", "Nikolovski", "Midfielder", 8));
        players.add(new Player("p6", "Darko", "Darkovski", "Forward", 11));
        players.add(new Player("p7", "Filip", "Filipovski", "Defender", 5));
        players.add(new Player("p8", "Martin", "Martinovski", "Midfielder", 10));
        players.add(new Player("p9", "Andrej", "Andrevski", "Defender", 3));
        players.add(new Player("p10", "David", "Davidovski", "Forward", 7));
    }
}

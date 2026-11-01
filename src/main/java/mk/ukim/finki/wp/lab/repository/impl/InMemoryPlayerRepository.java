package mk.ukim.finki.wp.lab.repository.impl;

import mk.ukim.finki.wp.lab.boostrap.DataHolder;
import mk.ukim.finki.wp.lab.model.Player;
import mk.ukim.finki.wp.lab.repository.PlayerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class InMemoryPlayerRepository implements PlayerRepository {

    @Override
    public List<Player> findAll() {
        return DataHolder.players;//ja vrakjame listata od igraci
    }

    @Override
    //data holder mesto kaj cuvae podatoci
    //stream so toa gi obrabotuvame podatocite
    //filter filtrirame
    //return DataHolder.player.stream().filter(object -> object.getIndex().equals(studentIndex)).findFirst();
    public Optional<Player> findByPlayerId(String playerId) { //vrakjame igrac cie id se sovpagja so toa sto go barame
        return DataHolder.players.stream().filter(player -> player.getPlayerId().equals(playerId)).findFirst();
    }
}

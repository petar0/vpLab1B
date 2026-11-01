package mk.ukim.finki.wp.lab.repository.impl;

import mk.ukim.finki.wp.lab.boostrap.DataHolder;
import mk.ukim.finki.wp.lab.model.Team;
import mk.ukim.finki.wp.lab.repository.TeamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryTeamRepository implements TeamRepository {


    @Override
    public List<Team> findAll() {
        return DataHolder.teams;//gi listamesite
    }

    @Override
    public Optional<Team> findById(Long id) {
        return DataHolder.teams.stream().filter(team -> team.getId().equals(id)).findFirst();
    }//go vrakja prviot team so toa id

    @Override
    public Team save(Team team) {
       Optional<Team> existhingTeam = findById(team.getId()); //proveruvame dali toj tim postoi
       if (existhingTeam.isPresent()) {
           //ja naogjae pozicijata na stario tim i zamenuvame
           int index = DataHolder.teams.indexOf(existhingTeam.get());
           DataHolder.teams.set(index, team);
       }else {
           DataHolder.teams.add(team);//dodaj go
       }
       return team;
    }
}
package de.lmoesle.processautomationexample.application.ports.out;

import de.lmoesle.processautomationexample.domain.user.TeamId;
import de.lmoesle.processautomationexample.domain.user.User;
import de.lmoesle.processautomationexample.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryOutPort {

    Optional<User> findById(UserId userId);

    List<User> findAllLeadsByTeamId(TeamId teamId);
}

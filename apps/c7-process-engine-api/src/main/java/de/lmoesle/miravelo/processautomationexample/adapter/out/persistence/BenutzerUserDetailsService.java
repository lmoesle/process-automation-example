package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
class BenutzerUserDetailsService implements UserDetailsService {

    private final BenutzerJpaRepository benutzerJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String benutzername) {
        // Demo-only user management: existing domain users double as Basic-Auth users.
        // A real system would delegate identity, credentials and account state to a dedicated IAM setup.
        return benutzerJpaRepository.findByBenutzername(benutzername)
            .filter(benutzer -> StringUtils.hasText(benutzer.getPasswortHash()))
            .map(benutzer -> User.withUsername(benutzer.getBenutzername())
                .password(benutzer.getPasswortHash())
                .roles("BENUTZER")
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + benutzername));
    }
}

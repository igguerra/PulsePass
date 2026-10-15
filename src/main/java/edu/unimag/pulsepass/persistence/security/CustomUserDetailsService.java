package edu.unimag.pulsepass.persistence.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.repository.UserCredentialRepository;

@Service 
public class CustomUserDetailsService implements UserDetailsService{

    private final UserCredentialRepository credentialRepository; 

    public CustomUserDetailsService(UserCredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository; 
    }

    @Override 
    @Transactional(readOnly = true)
    public SecurityPrincipal loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return credentialRepository.findByIdentifier(identifier)
                                    .map(SecurityPrincipal::from)
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found")); 
    }

    @Transactional(readOnly = true)
    public SecurityPrincipal loadUserById(Long userId) {
        return credentialRepository.findById(userId)
                                    .map(SecurityPrincipal::from)
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found")); 
    }

}

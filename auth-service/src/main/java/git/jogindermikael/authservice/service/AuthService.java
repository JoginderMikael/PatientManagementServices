package git.jogindermikael.authservice.service;

import git.jogindermikael.authservice.dto.LoginRequestDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    public Optional<String> authenticate(LoginRequestDto loginRequestDto) {


        return Optional.empty();
    }
}

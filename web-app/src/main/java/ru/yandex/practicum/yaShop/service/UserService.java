package ru.yandex.practicum.yaShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.yaShop.mapping.UserMapper;
import ru.yandex.practicum.yaShop.model.SecurityUserDetails;
import ru.yandex.practicum.yaShop.model.UserModel;
import ru.yandex.practicum.yaShop.repositories.TovarRepository;
import ru.yandex.practicum.yaShop.repositories.UserRepository;

@Service
public class UserService {

    public static final String UNKNOWN_USER = "UnknownUser";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    public Mono<UserModel> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .flatMap(authentication -> {
                    if (authentication == null || !authentication.isAuthenticated()) {
                        return Mono.empty();
                    }

                    Object principal = authentication.getPrincipal();
                    if (principal instanceof SecurityUserDetails) {
                        return Mono.just(userMapper.mapToModel((SecurityUserDetails) principal));
                    } else if (principal instanceof User) {
                        return userRepository.findByUsername(((User) principal).getUsername())
                                .map(userMapper::mapToModel);
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.empty())
                .onErrorResume(Exception -> Mono.empty());
    }
}

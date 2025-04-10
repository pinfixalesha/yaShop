package ru.yandex.practicum.yaShop.mapping;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.yaShop.entities.User;
import ru.yandex.practicum.yaShop.model.SecurityUserDetails;
import ru.yandex.practicum.yaShop.model.UserModel;

@Component
public class UserMapper {

    public UserModel mapToModel(SecurityUserDetails user){
        return UserModel.builder()
                .username(user.getUsername())
                .customerId(user.getCustomerId())
                .build();
    }

    public UserModel mapToModel(User user){
        return UserModel.builder()
                .username(user.getUsername())
                .customerId(user.getCustomerId())
                .build();
    }
}

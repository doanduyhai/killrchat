package com.datastax.demo.killrchat.configuration;

import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.manager.ChatRoomEntity_Manager;
import info.archinnov.achilles.generated.manager.MessageEntity_Manager;
import info.archinnov.achilles.generated.manager.PersistentTokenEntity_Manager;
import info.archinnov.achilles.generated.manager.UserEntity_Manager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration
public class AchillesConfiguration {

    @Inject
    ManagerFactory managerFactory;

    @Bean
    public UserEntity_Manager userEntityManager() {
        return managerFactory.forUserEntity();
    }

    @Bean
    public ChatRoomEntity_Manager chatRoomEntityManager() {
        return managerFactory.forChatRoomEntity();
    }

    @Bean
    public MessageEntity_Manager messageEntityManager() {
        return managerFactory.forMessageEntity();
    }

    @Bean
    public PersistentTokenEntity_Manager persistentTokenEntityManager() {
        return managerFactory.forPersistentTokenEntity();
    }
}

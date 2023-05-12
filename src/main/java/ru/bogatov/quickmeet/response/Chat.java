package ru.bogatov.quickmeet.response;

import ru.bogatov.quickmeet.entities.User;

import java.util.Set;
import java.util.UUID;

public class Chat {
    private UUID chatId;
    private Set<User> users;
    private User owner;
    private String name;


}

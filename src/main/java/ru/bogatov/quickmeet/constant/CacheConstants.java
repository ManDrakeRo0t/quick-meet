package ru.bogatov.quickmeet.constant;

import java.util.Set;

public class CacheConstants {
    public static final String USERS_CACHE = "users";
    public static final String MEET_CACHE = "meets";
    public static final String MEET_CATEGORY_CACHE = "meet_categories";
    public static final String MEET_LIST_OWNER_CACHE = "meets_list_owner_cache";
    public static final String MEET_LIST_GUEST_CACHE = "meets_list_guest_cache";

    public static final Set<String> CACHES_NAMES = Set.of(
            USERS_CACHE,
            MEET_CACHE,
            MEET_LIST_GUEST_CACHE,
            MEET_LIST_OWNER_CACHE
    );
}

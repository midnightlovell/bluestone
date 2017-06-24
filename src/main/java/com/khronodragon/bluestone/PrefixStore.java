package com.khronodragon.bluestone;

import com.j256.ormlite.dao.Dao;
import com.khronodragon.bluestone.sql.GuildPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PrefixStore {
    private static final Logger logger = LogManager.getLogger(PrefixStore.class);
    private String defaultPrefix = "!";
    private Dao<GuildPrefix, Long> dao;
    private Map<Long, String> cache = new HashMap<>();

    PrefixStore(Dao<GuildPrefix, Long> dao) {
        this.dao = dao;
    }

    PrefixStore(Dao<GuildPrefix, Long> dao, String defaultPrefix) {
        this.dao = dao;
        this.defaultPrefix = defaultPrefix;
    }

    public String getPrefix(long guildId) {
        if (cache.containsKey(guildId)) {
            return cache.get(guildId);
        } else {
            try {
                GuildPrefix result = dao.queryForId(guildId);
                if (result == null) {
                    cache.put(guildId, defaultPrefix);
                    return defaultPrefix;
                }

                String prefix = result.getPrefix();
                cache.put(guildId, prefix);

                return prefix;
            } catch (SQLException|NullPointerException e) {
                logger.error("Error getting prefix from DB", e);
                return defaultPrefix;
            }
        }
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public void setDefaultPrefix(String prefix) {
        defaultPrefix = prefix;
    }

    public Dao<GuildPrefix, Long> getDao() {
        return dao;
    }

    public void updateCache(long guildId, String prefix) {
        cache.put(guildId, prefix);
    }
}
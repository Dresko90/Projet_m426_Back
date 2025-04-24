package ch.epai.ict.m295.messaging.backend.domain;

import java.util.HashMap;
import java.util.Map;

public class IdGeneratorManager {
    
    private static Map<Class<?>, IdGenerator> idGeneratorMap = new HashMap<>();
    
    public static void register(IdGenerator idGenerator, Class<?> entityClass) {
        idGeneratorMap.put(entityClass, idGenerator);
    }

    public static IdGenerator get(Class<?> entityClass) {
        return idGeneratorMap.get(entityClass);
    }
}


package xyz.srnyx.vanadium.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class CodeManager {
    public static final Map<UUID, String> linkingCodes = new HashMap<>();

    public static String generateCode(UUID uuid) {
        final String code = String.valueOf(
                new Random().nextInt(9)) +
                new Random().nextInt(9) +
                new Random().nextInt(9) +
                new Random().nextInt(9);
        linkingCodes.put(uuid, code);
        return code;
    }

    /**
     * Remove a code
     */
    public static void removeCode(UUID uuid) {
        linkingCodes.remove(uuid);
    }

    /**
     * Gets the {@code UUID} associated with the specified {@code code}
     *
     * @return          The {@code UUID} associated with the {@code code}
     */
    public static UUID getUUIDFromCode(String code) {
        for (Map.Entry<UUID, String> entry : linkingCodes.entrySet()) if (entry.getValue().equals(code)) return entry.getKey();
        return null;
    }
}

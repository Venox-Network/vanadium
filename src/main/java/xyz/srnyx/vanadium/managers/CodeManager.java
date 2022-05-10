package xyz.srnyx.vanadium.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class CodeManager {
    private UUID uuid;
    private String code;

    public static final Map<UUID, String> linkingCodes = new HashMap<>();

    /**
     * Initializes a new {@code CodeManager}
     *
     * @param   object  The UUId/code to use for the methods
     */
    public CodeManager(Object object) {
        if (object instanceof UUID) {
            this.uuid = (UUID) object;
        } else if (object instanceof String) {
            this.code = (String) object;
        }
    }

    public String generateCode() {
        String code = String.valueOf(
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
    public void removeCode() {
        linkingCodes.remove(uuid);
    }

    /**
     * Gets the {@code UUID} associated with the specified {@code code}
     *
     * @return          The {@code UUID} associated with the {@code code}
     */
    public UUID getUUIDFromCode() {
        for (Map.Entry<UUID, String> entry : linkingCodes.entrySet()) if (entry.getValue().equals(code)) return entry.getKey();
        return null;
    }
}

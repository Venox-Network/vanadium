package xyz.srnyx.vanadium.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class CodeManager {
    @SuppressWarnings({"CanBeFinal"})
    private UUID uuid;

    @SuppressWarnings({"CanBeFinal"})
    public static Map<UUID, String> linkingCodes = new HashMap<>();

    /**
     * Initializes a new {@code CodeManager}
     *
     * @param   uuid    The UUID to use
     */
    public CodeManager(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Initializes a new {@code CodeManager} without any parameters
     */
    public CodeManager() {}

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
     * @param   code    The code to get the UUID from
     *
     * @return          The {@code UUID} associated with the {@code code}
     */
    public UUID getUUIDFromCode(String code) {
        for (Map.Entry<UUID, String> entry : linkingCodes.entrySet()) if (entry.getValue().equals(code)) {
            return entry.getKey();
        }
        return null;
    }
}

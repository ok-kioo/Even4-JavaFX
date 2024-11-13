package br.upe.persistence;

import br.upe.persistence.repository.Persistence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Attendee implements Persistence{
    private static final Logger LOGGER = Logger.getLogger(Attendee.class.getName());
    private static final String ATTENDEE_PATH = "./db/attendee.csv";
    private static final String WRITE_ERROR = "Erro na escrita do arquivo";
    private static final String SESSION_ID = "sessionId";
    private static final String USER_ID = "userId";
    private UUID id;
    private UUID userId;
    private String name;
    private UUID sessionId;


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void create(Object... params) {
        if (params.length < 2) {
            LOGGER.warning("Erro: Parâmetros insuficientes.");
            return;
        }

        this.userId = (UUID) params[0];
        this.name = (String) params[1];
        this.sessionId = (UUID) params[2];
        this.id = UUID.fromString(generateId());
        String line = id + ";" + userId + ";" + name + ";" + sessionId;

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ATTENDEE_PATH, true))) {
                writer.write(line);
                writer.newLine();
            }

            LOGGER.warning("Cadastro Realizado");
        } catch (IOException writerEx) {
            LOGGER.warning(WRITE_ERROR);
            writerEx.printStackTrace();
        }
    }


    @Override
    public void delete(Object... params) throws IOException {
        if (params.length > 1) {
            LOGGER.warning("Só pode ter 1 parametro");
        }

        HashMap<String, Persistence> attendeeHashMap = (HashMap<String, Persistence>) params[0];
        BufferedWriter writer = new BufferedWriter(new FileWriter(ATTENDEE_PATH));

        try (writer) {
            for (Map.Entry<String, Persistence> entry : attendeeHashMap.entrySet()) {
                Persistence attendee = entry.getValue();
                String line = attendee.getData("id") + ";" + attendee.getData(USER_ID) + ";" + attendee.getData("name") + ";" + attendee.getData(SESSION_ID) + "\n";
                writer.write(line);
            }
            LOGGER.warning("Inscrição Removida");
        } catch (IOException writerEx) {
            LOGGER.warning(WRITE_ERROR);
            writerEx.printStackTrace();
        } finally {
            writer.close();
        }
    }

    @Override
    public void update(Object... params) throws IOException {
        if (params.length > 1) {
            LOGGER.warning("Só pode ter 1 parametro");
        }

        HashMap<String, Persistence> attendeeHashMap = (HashMap<String, Persistence>) params[0];
        BufferedWriter writer = new BufferedWriter(new FileWriter(ATTENDEE_PATH));
        try (writer) {
            for (Map.Entry<String, Persistence> entry : attendeeHashMap.entrySet()) {
                Persistence attendee = entry.getValue();
                String line = attendee.getData("id") + ";" + attendee.getData(USER_ID) + ";" + attendee.getData("name") + ";" + attendee.getData(SESSION_ID) + "\n";
                writer.write(line);
            }
            LOGGER.warning("Nome Atualizado");
        } catch (IOException writerEx) {
            LOGGER.warning(WRITE_ERROR);
            writerEx.printStackTrace();
        } finally {
            writer.close();
        }
    }

    @Override
    public Object getData(String dataToGet) {
        Object data = new Object();
        try {
            switch (dataToGet) {
                case "name" -> data = this.getName();
                case SESSION_ID -> data = this.getSessionId();
                case USER_ID -> data = this.getUserId();
                case "id" -> data = this.getId();
                default -> throw new IOException();
            }
        } catch (IOException e) {
            LOGGER.warning("Informação não existe ou é restrita");
        }
        return data;
    }

    @Override
    public Object getData(UUID eventId, String dataToGet) {
        return null;
    }

    @Override
    public void setData(UUID eventId, String dataToSet, Object data) {

    }


    @Override
    public void setData(String dataToSet, Object data) {
        try {
            switch (dataToSet) {
                case "name" -> this.setName((String) data);
                case SESSION_ID -> this.setSessionId((UUID) data);
                case "id" -> this.setId((UUID) data);
                case USER_ID -> this.setUserId((UUID) data);
                default -> throw new IOException();
            }
        } catch (IOException e) {
            LOGGER.warning("Informação não existe ou é restrita");
        }
    }

    private String generateId() {
        SecureRandom secureRandom = new SecureRandom();
        long timestamp = Instant.now().toEpochMilli();
        int lastThreeDigitsOfTimestamp = (int) (timestamp % 1000);
        int randomValue = secureRandom.nextInt(900) + 100;
        return String.format("%03d%03d", lastThreeDigitsOfTimestamp, randomValue);
    }

    @Override
    public HashMap<UUID, Persistence> read() throws IOException {
        HashMap<UUID, Persistence> list = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ATTENDEE_PATH), StandardCharsets.UTF_8));
        try (reader) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String parsedId = parts[0].trim();
                    String parsedUserId = parts[1].trim();
                    String parsedName = parts[2].trim();
                    String parsedSessionId = parts[3].trim();

                    Attendee attendee = new Attendee();
                    attendee.setName(parsedName);
                    attendee.setSessionId(UUID.fromString(parsedSessionId));
                    attendee.setId(UUID.fromString(parsedId));
                    attendee.setUserId(UUID.fromString(parsedUserId));
                    list.put(attendee.getId(), attendee);
                }
            }

        } catch (IOException readerEx) {
            LOGGER.warning("Erro ao ler o arquivo");
            readerEx.printStackTrace();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } finally {
            reader.close();
        }
        return list;
    }

    @Override
    public HashMap<UUID, Persistence> read(Object... params) {
        return new HashMap<>();
    }

    @Override
    public boolean loginValidate(String email, String password) {
        return false;
    }
}

package br.upe.persistence.repository;

import br.upe.persistence.Attendee;
import br.upe.persistence.Session;
import br.upe.persistence.User;
import br.upe.persistence.builder.AttendeeBuilder;
import br.upe.utils.JPAUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class AttendeeRepository implements Persistence {
    private static final Logger LOGGER = Logger.getLogger(AttendeeRepository.class.getName());
    private static AttendeeRepository instance;

    public AttendeeRepository() {
        // Construtor vazio porque esta classe não requer inicialização específica.
    }

    public static AttendeeRepository getInstance() {
        if (instance == null) {
            synchronized (AttendeeRepository.class) {
                instance = new AttendeeRepository();
            }
        }
        return instance;
    }

    public List<Attendee> getAllAttendees() {
        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        TypedQuery<Attendee> query = entityManager.createQuery("SELECT e FROM Attendee e", Attendee.class);
        return query.getResultList();
    }

    @Override
    public Object[] create(Object... params) {
        if (params.length != 2) {
            LOGGER.warning("Só pode ter 2 parâmetros");
            return new Object[]{false, null};
        }

        UUID parsedUserId = UUID.fromString((String) params[0]);
        UUID parsedSessionId = UUID.fromString((String) params[1]);
        boolean isCreated = false;
        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        EntityTransaction transaction = entityManager.getTransaction();

        Attendee attendee = null;
        try {
            transaction.begin();

            User user = entityManager.find(User.class, parsedUserId);
            if (user == null) {
                throw new IllegalArgumentException("Usuário não encontrado com o ID: " + parsedUserId);
            }

            Session session = entityManager.find(Session.class, parsedSessionId);
            if (session == null) {
                throw new IllegalArgumentException("Sessão não encontrada com o ID: " + parsedSessionId);
            }

            attendee = entityManager.createQuery(
                            "SELECT a FROM Attendee a WHERE a.userId = :userId", Attendee.class)
                    .setParameter("userId", user)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (attendee == null) {
                attendee = AttendeeBuilder.builder()
                        .withUser(user)
                        .build();
                attendee = entityManager.merge(attendee);
            } else {
                attendee = entityManager.merge(attendee);
            }

            session = entityManager.merge(session);
            attendee.addSession(session);

            entityManager.merge(attendee);

            transaction.commit();
            LOGGER.info("Sessão adicionada ao Attendee: " + attendee.getId());
            isCreated = true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Erro ao commitar a transação: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        assert attendee != null;
        return new Object[]{isCreated, attendee.getId()};
    }



    @Override
    public boolean delete(Object... params) throws IOException {
        if (params.length != 2) {
            LOGGER.warning("Só pode ter 2 parametros");
            return false;
        }

        UUID attendeeId = (UUID) params[0];
        UUID sessionId = (UUID) params[1];
        boolean isDeleted = false;
        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Attendee attendee = entityManager.find(Attendee.class, attendeeId);
            Session session = entityManager.find(Session.class, sessionId);

            if (attendee == null) {
                LOGGER.warning("Participante não encontrado com o ID fornecido.");
                return false;
            }

            if (session == null) {
                LOGGER.warning("Sessão não encontrada com o ID fornecido.");
                return false;
            }

            if (attendee.getSessions().contains(session)) {
                attendee.getSessions().remove(session);
                entityManager.merge(attendee);
                transaction.commit();
                LOGGER.info("Participação removida com sucesso.");
                isDeleted = true;
            } else {
                LOGGER.warning("Participante não está inscrito na sessão fornecida.");
            }
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("Erro ao remover participação: " + e.getMessage());
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return isDeleted;
    }

    @Override
    public boolean update(Object... params) throws IOException {
        return false;
    }

    @Override
    public Object getData(UUID id, String dataToGet) {
        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        Attendee attendee = entityManager.find(Attendee.class, id);
        if (attendee == null) {
            return null;
        }
        return switch (dataToGet) {
            case "id" -> attendee.getId();
            case "name" -> attendee.getName();
            case "userId" -> attendee.getUserId();
            case "sessionIds" -> attendee.getSessionIds();
            default -> null;
        };
    }

    @Override
    public void setData(UUID eventId, String dataToSet, Object data) {
        // classe não necessita desse metodo
    }

    @Override
    public Object getData(String dataToGet) {
        return null;
    }

    @Override
    public Object[] isExist(Object... params) throws IOException {
        if (params.length != 1) {
            LOGGER.warning("Só pode ter 1 parametro");
            return new Object[]{false, null};
        }

        String userId = (String) params[0];

        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        TypedQuery<Attendee> query = entityManager.createQuery(
                "SELECT e FROM Attendee e WHERE e.userId.id = :userId", Attendee.class);
        query.setParameter("userId", UUID.fromString(userId));
        try {
            Attendee attendee = query.getSingleResult();
            return new Object[]{true, attendee.getId()};
        } catch (Exception e) {
            LOGGER.warning("Participante não encontrado.");
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return new Object[]{false, null};
    }
}

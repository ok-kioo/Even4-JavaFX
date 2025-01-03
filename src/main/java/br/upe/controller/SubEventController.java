package br.upe.controller;

import br.upe.persistence.Event;
import br.upe.persistence.SubEvent;
import br.upe.persistence.repository.Persistence;
import br.upe.persistence.repository.SubEventRepository;
import br.upe.utils.JPAUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SubEventController implements Controller {
    private static final String DESCRIPTION = "description";
    private static final String LOCATION = "location";
    private static final String OWNER_ID = "ownerId";
    private static final String EVENT_ID = "eventId";
    private static final Logger LOGGER = Logger.getLogger(SubEventController.class.getName());
    private Persistence subEventLog;

    public SubEventController() throws IOException {
        //
    }

    @Override
    public <T> List<T> getAll() {
        SubEventRepository subeventRepository = SubEventRepository.getInstance();
        return (List<T>) subeventRepository.getAllSubEvents();
    }

    @Override
    public <T> List<T> getEventArticles(UUID eventId) {
        return List.of();
    }

    @Override
    public <T> List<T> list(Object... params) throws IOException {
        UUID userId = UUID.fromString((String) params[0]);
        SubEventRepository subeventRepository = SubEventRepository.getInstance();
        List<SubEvent> allSubEvents = subeventRepository.getAllSubEvents();
        List<SubEvent> userSubEvents = new ArrayList<>();

        for (SubEvent subevent : allSubEvents) {
            if (subevent.getOwnerId().getId().equals(userId)) {
                userSubEvents.add(subevent);
            }
        }

        if (userSubEvents.isEmpty()) {
            LOGGER.warning("Seu usuário atual é organizador de nenhum Subevento");
        }

        return (List<T>) userSubEvents;
    }

    @Override
    public Object[] create(Object... params) throws IOException {
        if (params.length != 6) {
            LOGGER.warning("Só pode ter 6 parâmetros");
            return new Object[]{false, null};
        }

        String fatherEventId = getFatherEventId((String) params[0]);
        String name = (String) params[1];
        Date date = (Date) params[2];
        String description = (String) params[3];
        String location = (String) params[4];
        String userId = (String) params[5];

        Persistence subEvent = new SubEventRepository();
        return subEvent.create(fatherEventId, name, date, description, location, userId);
    }


    @Override
    public boolean delete(Object... params) throws IOException {
        if (params.length != 2) {
            LOGGER.warning("Só pode ter 2 parametro");
            return false;
        }

        SubEventRepository subeventRepository = SubEventRepository.getInstance();

        UUID id = (UUID) params[0];
        UUID ownerId = UUID.fromString((String) params[1]);

        return subeventRepository.delete(id, ownerId);
    }


    @Override
    public boolean update(Object... params) throws IOException {
        if (!isValidParamsLength(params)) {
            LOGGER.warning("Só pode ter 5 parametros");
            return false;
        }

        UUID id = (UUID) params[0];
        String newName = (String) params[1];
        Date newDate = (Date) params[2];
        String newDescription = (String) params[3];
        String newLocation = (String) params[4];


        return updateSubEvent(id, newName, newDate, newDescription, newLocation);
    }

    private boolean updateSubEvent(UUID id, String newName, Date newDate, String newDescription, String newLocation) throws IOException {
        boolean isUpdated = false;
        if (id != null) {
            SubEventRepository subeventRepository = SubEventRepository.getInstance();
            isUpdated = subeventRepository.update(id, newName, newDate, newDescription, newLocation);
        } else {
            LOGGER.warning("Evento não encontrado");
        }
        return isUpdated;
    }

    @Override
    public String getData(String dataToGet) {
        String data = "";
        try {
            switch (dataToGet) {
                case "id" -> data = (String) this.subEventLog.getData("id");
                case "name" -> data = (String) this.subEventLog.getData("name");
                case DESCRIPTION -> data = (String) this.subEventLog.getData(DESCRIPTION);
                case "date" -> data = String.valueOf(this.subEventLog.getData("date"));
                case LOCATION -> data = (String) this.subEventLog.getData(LOCATION);
                case EVENT_ID -> data = (String) this.subEventLog.getData(EVENT_ID);
                case OWNER_ID -> data = (String) this.subEventLog.getData(OWNER_ID);
                default -> throw new IOException();
            }
        } catch (IOException e) {
            LOGGER.warning("Informação não existe ou é restrita");
        }
        return data;
    }

    @Override
    public String[] verifyByEventName(String eventName) {
        return new String[0];
    }

    @Override
    public String[] verifyBySessionName(String sessionName) {
        return new String[0];
    }

    private boolean isValidParamsLength(Object... params) {
        return params.length == 5;
    }

    private String getFatherEventId(String searchName) {
        EntityManager entityManager = JPAUtils.getEntityManagerFactory();
        String fatherId = null;
        try {
            TypedQuery<Event> query = entityManager.createQuery("SELECT e FROM Event e WHERE e.name = :searchName", Event.class);
            query.setParameter("searchName", searchName);
            Event event = query.getSingleResult();
            fatherId = event.getId().toString();
        } catch (NoResultException e) {
            LOGGER.warning("Evento pai não encontrado\n");
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return fatherId;
    }

    @Override
    public Object[] isExist(Object... params) throws IOException {
        if (params.length != 2) {
            LOGGER.warning("Só pode ter 2 parâmetro");
            return new Object[]{false, null};
        }
        SubEventRepository subeventRepository = SubEventRepository.getInstance();
        String name = (String) params[0];
        String ownerId = (String) params[1];
        return subeventRepository.isExist(name, ownerId);
    }
}
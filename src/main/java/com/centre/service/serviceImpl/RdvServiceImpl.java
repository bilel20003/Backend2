package com.centre.service.serviceImpl;

import com.centre.service.model.Rdv;
import com.centre.service.model.UserInfo;
import com.centre.service.repository.RdvRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.service.RdvService;
import com.centre.service.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RdvServiceImpl implements RdvService {

    private static final Logger log = LoggerFactory.getLogger(RdvServiceImpl.class);

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ScheduleService scheduleService;

    private UserInfo findTechnicianWithLeastRdvs() {
        List<UserInfo> technicians = userInfoRepository.findByRoleNameAndArchiverFalse("TECHNICIEN");
        UserInfo selectedTechnician = null;
        long minRdvs = Long.MAX_VALUE;

        for (UserInfo technician : technicians) {
            long rdvCount = rdvRepository.countRdvsByTechnicienId(technician.getId());
            if (rdvCount < minRdvs) {
                minRdvs = rdvCount;
                selectedTechnician = technician;
            }
        }
        return selectedTechnician;
    }

    @Override
    public ResponseEntity<?> addRdv(Rdv rdv) {
        try {
            // Validate client
            if (rdv.getClient() == null || rdv.getClient().getId() == null) {
                log.error("Client is missing or has no ID");
                return new ResponseEntity<>("{\"message\":\"L'objet client est requis\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> clientOpt = userInfoRepository.findByIdAndArchiverFalse(rdv.getClient().getId());
            if (clientOpt.isEmpty() || !clientOpt.get().getRole().getName().equals("CLIENT")) {
                log.error("Client with ID {} not found, archived, or not a CLIENT", rdv.getClient().getId());
                return new ResponseEntity<>("{\"message\":\"Client non trouvé, archivé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }
            rdv.setClient(clientOpt.get());

            // Check client RDV limit
            long pendingRdvs = rdvRepository.countPendingRdvsByClientId(rdv.getClient().getId());
            if (pendingRdvs >= 3) {
                log.error("Client ID {} has reached the maximum of 3 pending RDVs", rdv.getClient().getId());
                return new ResponseEntity<>("{\"message\":\"Limite de 3 rendez-vous en attente atteinte\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Validate dateSouhaitee
            if (rdv.getDateSouhaitee() == null) {
                log.error("DateSouhaitee is required");
                return new ResponseEntity<>("{\"message\":\"Date souhaitée est requise\"}", HttpStatus.BAD_REQUEST);
            }

            // Prevent same-day or past appointments
            LocalDate today = LocalDate.now();
            LocalDate rdvDate = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!rdvDate.isAfter(today)) {
                log.error("Appointment not allowed for today or past date {}", rdvDate);
                return new ResponseEntity<>(
                        "{\"message\":\"Les rendez-vous pour aujourd'hui ou dans le passé ne sont pas autorisés\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Check if the slot is available
            LocalTime time = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                    .truncatedTo(java.time.temporal.ChronoUnit.HOURS);
            String dayOfWeek = rdvDate.getDayOfWeek().toString();
            List<LocalTime> availableSlots = scheduleService.getAvailableSlots(dayOfWeek, rdvDate);

            if (!availableSlots.contains(time)) {
                log.error("Selected time slot {} on {} is not available", time, rdvDate);
                return new ResponseEntity<>("{\"message\":\"Le créneau horaire sélectionné n'est pas disponible\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Check for double-booking
            java.sql.Timestamp startOfSlot = java.sql.Timestamp.valueOf(rdvDate.atTime(time));
            java.sql.Timestamp endOfSlot = java.sql.Timestamp.valueOf(rdvDate.atTime(time.plusHours(1)));
            List<Rdv> existingRdvs = rdvRepository.findByArchiverFalse().stream()
                    .filter(r -> r.getDateSouhaitee().after(startOfSlot) && r.getDateSouhaitee().before(endOfSlot))
                    .collect(Collectors.toList());
            if (!existingRdvs.isEmpty()) {
                log.error("Time slot {} on {} is already booked", time, rdvDate);
                return new ResponseEntity<>("{\"message\":\"Ce créneau est déjà réservé\"}", HttpStatus.CONFLICT);
            }

            // Assign technician
            UserInfo technician = findTechnicianWithLeastRdvs();
            if (technician == null) {
                log.error("No available technicians found");
                return new ResponseEntity<>("{\"message\":\"Aucun technicien disponible\"}", HttpStatus.BAD_REQUEST);
            }
            rdv.setTechnicien(technician);

            // Set default values
            rdv.setStatus("EN_ATTENTE");
            rdv.setDateEnvoi(new Date());
            rdv.setArchiver(false);

            // Save the appointment
            rdvRepository.save(rdv);
            log.info("Rendez-vous ajouté avec succès : {}", rdv);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous ajouté avec succès\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du rendez-vous : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'ajout du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRdvs() {
        try {
            List<Rdv> rdvs = rdvRepository.findByArchiverFalse();
            return new ResponseEntity<>(rdvs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rendez-vous : {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRdvById(Long id) {
        try {
            Optional<Rdv> rdvOpt = rdvRepository.findByIdAndArchiverFalse(id);
            if (rdvOpt.isEmpty()) {
                log.warn("Rendez-vous avec l'ID {} non trouvé ou archivé", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé ou archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            Rdv rdv = rdvOpt.get();
            return new ResponseEntity<>(rdv, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du rendez-vous ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRdvsByClient(Long clientId) {
        try {
            // Validate client
            Optional<UserInfo> clientOpt = userInfoRepository.findByIdAndArchiverFalse(clientId);
            if (clientOpt.isEmpty() || !clientOpt.get().getRole().getName().equals("CLIENT")) {
                log.error("Client with ID {} not found, archived, or not a CLIENT", clientId);
                return new ResponseEntity<>("{\"message\":\"Client non trouvé, archivé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }

            List<Rdv> rdvs = rdvRepository.findByClientIdAndArchiverFalse(clientId);
            log.info("Retrieved {} rendez-vous for client ID {}", rdvs.size(), clientId);
            return new ResponseEntity<>(rdvs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rendez-vous pour client ID {} : {}", clientId, e.getMessage(),
                    e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRdvsByTechnicien(Long technicienId) {
        try {
            // Validate technician
            Optional<UserInfo> technicienOpt = userInfoRepository.findByIdAndArchiverFalse(technicienId);
            if (technicienOpt.isEmpty() || !technicienOpt.get().getRole().getName().equals("TECHNICIEN")) {
                log.error("Technicien with ID {} not found, archived, or not a TECHNICIEN", technicienId);
                return new ResponseEntity<>("{\"message\":\"Technicien non trouvé, archivé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }

            List<Rdv> rdvs = rdvRepository.findByTechnicienIdAndArchiverFalse(technicienId);
            log.info("Retrieved {} rendez-vous for technicien ID {}", rdvs.size(), technicienId);
            return new ResponseEntity<>(rdvs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rendez-vous pour technicien ID {} : {}", technicienId,
                    e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateRdv(Long id, Rdv updatedRdv) {
        try {
            Optional<Rdv> existingRdvOpt = rdvRepository.findByIdAndArchiverFalse(id);
            if (existingRdvOpt.isEmpty()) {
                log.warn("Rendez-vous avec l'ID {} non trouvé ou archivé", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé ou archivé\"}",
                        HttpStatus.NOT_FOUND);
            }

            Rdv existingRdv = existingRdvOpt.get();
            boolean isUpdated = false;

            // Validate client if provided
            if (updatedRdv.getClient() != null && updatedRdv.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository
                        .findByIdAndArchiverFalse(updatedRdv.getClient().getId());
                if (clientOpt.isEmpty() || !clientOpt.get().getRole().getName().equals("CLIENT")) {
                    log.error("Client with ID {} not found, archived, or not a CLIENT", updatedRdv.getClient().getId());
                    return new ResponseEntity<>("{\"message\":\"Client non trouvé, archivé ou rôle incorrect\"}",
                            HttpStatus.NOT_FOUND);
                }
                existingRdv.setClient(clientOpt.get());
                isUpdated = true;
            }

            // Validate technician if provided
            if (updatedRdv.getTechnicien() != null && updatedRdv.getTechnicien().getId() != null) {
                Optional<UserInfo> technicienOpt = userInfoRepository
                        .findByIdAndArchiverFalse(updatedRdv.getTechnicien().getId());
                if (technicienOpt.isEmpty() || !technicienOpt.get().getRole().getName().equals("TECHNICIEN")) {
                    log.error("Technicien with ID {} not found, archived, or not a TECHNICIEN",
                            updatedRdv.getTechnicien().getId());
                    return new ResponseEntity<>("{\"message\":\"Technicien non trouvé, archivé ou rôle incorrect\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRdv.setTechnicien(technicienOpt.get());
                isUpdated = true;
            }

            // Update other fields
            if (updatedRdv.getDateSouhaitee() != null) {
                LocalDate today = LocalDate.now();
                LocalDate rdvDate = updatedRdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (!rdvDate.isAfter(today)) {
                    log.error("Appointment not allowed for today or past date {}", rdvDate);
                    return new ResponseEntity<>(
                            "{\"message\":\"Les rendez-vous pour aujourd'hui ou dans le passé ne sont pas autorisés\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRdv.setDateSouhaitee(updatedRdv.getDateSouhaitee());
                isUpdated = true;
            }

            if (updatedRdv.getDescription() != null && !updatedRdv.getDescription().trim().isEmpty()) {
                existingRdv.setDescription(updatedRdv.getDescription());
                isUpdated = true;
            }

            if (updatedRdv.getStatus() != null && !updatedRdv.getStatus().trim().isEmpty()) {
                if (!updatedRdv.getStatus().equals("EN_ATTENTE") && !updatedRdv.getStatus().equals("TERMINE")
                        && !updatedRdv.getStatus().equals("REFUSE")) {
                    log.error("Invalid status {} for RDV ID {}", updatedRdv.getStatus(), id);
                    return new ResponseEntity<>(
                            "{\"message\":\"Statut invalide. Valeurs autorisées: EN_ATTENTE, TERMINE, REFUSE\"}",
                            HttpStatus.BAD_REQUEST);
                }
                // Check client RDV limit if updating to EN_ATTENTE
                if (updatedRdv.getStatus().equals("EN_ATTENTE") && !existingRdv.getStatus().equals("EN_ATTENTE")) {
                    long pendingRdvs = rdvRepository.countPendingRdvsByClientId(existingRdv.getClient().getId());
                    if (pendingRdvs >= 3) {
                        log.error("Client ID {} has reached the maximum of 3 pending RDVs",
                                existingRdv.getClient().getId());
                        return new ResponseEntity<>("{\"message\":\"Limite de 3 rendez-vous en attente atteinte\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                }
                existingRdv.setStatus(updatedRdv.getStatus());
                isUpdated = true;
            }

            if (updatedRdv.getNoteRetour() != null) {
                existingRdv.setNoteRetour(updatedRdv.getNoteRetour());
                isUpdated = true;
            }

            if (updatedRdv.getTypeProbleme() != null && !updatedRdv.getTypeProbleme().trim().isEmpty()) {
                existingRdv.setTypeProbleme(updatedRdv.getTypeProbleme());
                isUpdated = true;
            }

            if (updatedRdv.getDateEnvoi() != null) {
                existingRdv.setDateEnvoi(updatedRdv.getDateEnvoi());
                isUpdated = true;
            }

            // Validate and update meetLink
            if (updatedRdv.getMeetLink() != null && !updatedRdv.getMeetLink().trim().isEmpty()) {
                String meetLink = updatedRdv.getMeetLink().trim();
                // Validate meetLink format
                if (!meetLink.matches("^https://meet\\.google\\.com/[a-z]{3}-[a-z]{4}-[a-z]{3}$")) {
                    log.error("Invalid meetLink format for RDV ID {}: {}", id, meetLink);
                    return new ResponseEntity<>("{\"message\":\"Format du lien Meet invalide\"}",
                            HttpStatus.BAD_REQUEST);
                }
                // Check for uniqueness
                Optional<Rdv> existingRdvWithLink = rdvRepository.findByMeetLink(meetLink);
                if (existingRdvWithLink.isPresent() && !existingRdvWithLink.get().getId().equals(id)) {
                    log.error("meetLink {} already used by another RDV ID {}", meetLink,
                            existingRdvWithLink.get().getId());
                    return new ResponseEntity<>("{\"message\":\"Lien Meet déjà utilisé par un autre rendez-vous\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRdv.setMeetLink(meetLink);
                isUpdated = true;
            }

            if (!isUpdated) {
                log.warn("Aucun champ valide fourni pour la mise à jour du rendez-vous ID {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }

            existingRdv.setArchiver(false);
            rdvRepository.save(existingRdv);
            log.info("Rendez-vous mis à jour avec succès pour l'ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous mis à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du rendez-vous ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la mise à jour du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveRdv(Long id) {
        try {
            Optional<Rdv> rdvOpt = rdvRepository.findByIdAndArchiverFalse(id);
            if (rdvOpt.isEmpty()) {
                log.warn("Rendez-vous avec l'ID {} non trouvé ou déjà archivé", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé ou déjà archivé\"}",
                        HttpStatus.NOT_FOUND);
            }
            Rdv rdv = rdvOpt.get();
            rdv.setArchiver(true);
            rdvRepository.save(rdv);
            log.info("Rendez-vous archivé avec succès pour l'ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous archivé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de l'archivage du rendez-vous ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'archivage du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> refuseRdv(Long id, Long technicienId, String noteRetour) {
        try {
            Optional<UserInfo> technicienOpt = userInfoRepository.findByIdAndArchiverFalse(technicienId);
            if (technicienOpt.isEmpty() || !technicienOpt.get().getRole().getName().equals("TECHNICIEN")) {
                log.error("Technicien with ID {} not found, archived, or not a TECHNICIEN", technicienId);
                return new ResponseEntity<>("{\"message\":\"Technicien non trouvé, archivé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }

            Optional<Rdv> rdvOpt = rdvRepository.findByIdAndArchiverFalse(id);
            if (rdvOpt.isEmpty()) {
                log.warn("Rendez-vous avec l'ID {} non trouvé ou archivé", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé ou archivé\"}",
                        HttpStatus.NOT_FOUND);
            }

            Rdv rdv = rdvOpt.get();
            rdv.setStatus("REFUSE");
            rdv.setNoteRetour(noteRetour != null ? noteRetour.trim() : null); // Enregistrer noteRetour, peut être null

            rdvRepository.save(rdv);
            log.info("Rendez-vous refusé avec succès pour l'ID: {} par technicien: {} avec noteRetour: {}",
                    id, technicienId, noteRetour);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous refusé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors du refus du rendez-vous ID {} : {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors du refus du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
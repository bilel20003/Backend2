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

    @Override
    public ResponseEntity<?> addRdv(Rdv rdv) {
        try {
            // Validate client
            if (rdv.getClient() == null || rdv.getClient().getId() == null) {
                return new ResponseEntity<>("{\"message\":\"L'objet client est requis\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<UserInfo> clientOpt = userInfoRepository.findById(rdv.getClient().getId());
            if (clientOpt.isEmpty() || !clientOpt.get().getRole().getName().equals("CLIENT")) {
                return new ResponseEntity<>("{\"message\":\"Client non trouvé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }

            // Validate dateSouhaitee
            if (rdv.getDateSouhaitee() == null) {
                return new ResponseEntity<>("{\"message\":\"Date souhaitée est requise\"}", HttpStatus.BAD_REQUEST);
            }

            // Check if the slot is available
            LocalDate date = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime time = rdv.getDateSouhaitee().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                    .truncatedTo(java.time.temporal.ChronoUnit.HOURS); // Round to hour
            String dayOfWeek = date.getDayOfWeek().toString();
            List<LocalTime> availableSlots = scheduleService.getAvailableSlots(dayOfWeek, date);

            if (!availableSlots.contains(time)) {
                return new ResponseEntity<>("{\"message\":\"Le créneau horaire sélectionné n'est pas disponible\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Check for double-booking
            java.sql.Timestamp startOfSlot = java.sql.Timestamp.valueOf(date.atTime(time));
            java.sql.Timestamp endOfSlot = java.sql.Timestamp.valueOf(date.atTime(time.plusHours(1)));
            List<Rdv> existingRdvs = rdvRepository.findAll().stream()
                    .filter(r -> r.getDateSouhaitee().after(startOfSlot) && r.getDateSouhaitee().before(endOfSlot))
                    .collect(Collectors.toList());
            if (!existingRdvs.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Ce créneau est déjà réservé\"}", HttpStatus.CONFLICT);
            }

            // Set client and dateEnvoi
            rdv.setClient(clientOpt.get());
            rdv.setDateEnvoi(new java.util.Date());

            // Save the appointment
            rdvRepository.save(rdv);
            log.info("Rendez-vous ajouté avec succès : {}", rdv);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous ajouté avec succès\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du rendez-vous : {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'ajout du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRdvs() {
        try {
            List<Rdv> rdvs = rdvRepository.findAll();
            return new ResponseEntity<>(rdvs, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rendez-vous : {}", e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateRdv(Long id, Rdv updatedRdv) {
        try {
            // Vérifiez si le rendez-vous existe
            Optional<Rdv> existingRdvOpt = rdvRepository.findById(id);
            if (existingRdvOpt.isEmpty()) {
                log.warn("Tentative de mise à jour d'un rendez-vous qui n'existe pas avec l'ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé\"}", HttpStatus.NOT_FOUND);
            }

            // Récupérez le rendez-vous actuel
            Rdv existingRdv = existingRdvOpt.get();

            // Vérifiez si le client est fourni dans updatedRdv
            if (updatedRdv.getClient() != null && updatedRdv.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository.findById(updatedRdv.getClient().getId());
                if (clientOpt.isPresent() && clientOpt.get().getRole().getName().equals("CLIENT")) {
                    existingRdv.setClient(clientOpt.get());
                } else {
                    return new ResponseEntity<>("{\"message\":\"Client non trouvé ou rôle incorrect\"}",
                            HttpStatus.NOT_FOUND);
                }
            }

            // Mettez à jour les autres champs
            boolean isUpdated = false;

            if (updatedRdv.getDateSouhaitee() != null) {
                existingRdv.setDateSouhaitee(updatedRdv.getDateSouhaitee());
                isUpdated = true;
            }

            if (updatedRdv.getDescription() != null && !updatedRdv.getDescription().trim().isEmpty()) {
                existingRdv.setDescription(updatedRdv.getDescription());
                isUpdated = true;
            }

            if (updatedRdv.getStatus() != null && !updatedRdv.getStatus().trim().isEmpty()) {
                existingRdv.setStatus(updatedRdv.getStatus());
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

            // Si aucune donnée n'a été mise à jour, renvoie un message d'erreur
            if (!isUpdated) {
                log.warn("Aucun champ valide fourni pour la mise à jour du rendez-vous avec l'ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }

            // Sauvegarde le rendez-vous mis à jour
            rdvRepository.save(existingRdv);
            log.info("Rendez-vous mis à jour avec succès pour l'ID: {}", id);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous mis à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du rendez-vous avec l'ID: {}. Erreur: {}", id, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la mise à jour du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteRdv(Long id) {
        try {
            if (rdvRepository.existsById(id)) {
                rdvRepository.deleteById(id);
                log.info("Rendez-vous supprimé avec succès pour l'ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous supprimé avec succès\"}", HttpStatus.OK);
            } else {
                log.warn("Tentative de suppression d'un rendez-vous qui n'existe pas avec l'ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé\"}", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du rendez-vous avec l'ID: {}. Erreur: {}", id, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la suppression du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> refuseRdv(Long id, Long guichetierId) {
        try {
            // Verify guichetier
            Optional<UserInfo> guichetierOpt = userInfoRepository.findById(guichetierId);
            if (guichetierOpt.isEmpty() || !guichetierOpt.get().getRole().getName().equals("GUICHETIER")) {
                return new ResponseEntity<>("{\"message\":\"Guichetier non trouvé ou rôle incorrect\"}",
                        HttpStatus.NOT_FOUND);
            }

            // Verify RDV
            Optional<Rdv> rdvOpt = rdvRepository.findById(id);
            if (rdvOpt.isEmpty()) {
                log.warn("Tentative de refus d'un rendez-vous qui n'existe pas avec l'ID: {}", id);
                return new ResponseEntity<>("{\"message\":\"Rendez-vous non trouvé\"}", HttpStatus.NOT_FOUND);
            }

            // Update status to REFUSE
            Rdv rdv = rdvOpt.get();
            rdv.setStatus("REFUSE");
            rdvRepository.save(rdv);
            log.info("Rendez-vous refusé avec succès pour l'ID: {} par guichetier: {}", id, guichetierId);
            return new ResponseEntity<>("{\"message\":\"Rendez-vous refusé avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erreur lors du refus du rendez-vous avec l'ID: {}. Erreur: {}", id, e.getMessage());
            return new ResponseEntity<>("{\"message\":\"Erreur lors du refus du rendez-vous\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
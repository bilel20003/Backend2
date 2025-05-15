package com.centre.service.serviceImpl;

import com.centre.service.model.EtatRequete;
import com.centre.service.model.Requete;
import com.centre.service.model.UserInfo;
import com.centre.service.model.Objet;
import com.centre.service.model.PieceJointe;
import com.centre.service.repository.RequeteRepository;
import com.centre.service.repository.UserInfoRepository;
import com.centre.service.repository.ObjetRepository;
import com.centre.service.repository.PieceJointeRepository;
import com.centre.service.service.RequeteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequeteServiceImpl implements RequeteService {

    private static final Logger log = LoggerFactory.getLogger(RequeteServiceImpl.class);

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    @Autowired
    private RequeteRepository requeteRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ObjetRepository objetRepository;

    @Autowired
    private PieceJointeRepository pieceJointeRepository;

    @Override
    public ResponseEntity<?> addRequete(Requete requete, MultipartFile[] files) {
        try {
            log.info("Received request to add requete: {}", requete);
            if (requete.getObjet() == null || requete.getObjet().getId() == null) {
                log.error("Objet is missing or has no ID");
                return new ResponseEntity<>("{\"message\":\"Objet is required\"}", HttpStatus.BAD_REQUEST);
            }
            Optional<Objet> objetOpt = objetRepository.findByIdAndArchiverFalse(requete.getObjet().getId());
            if (objetOpt.isEmpty()) {
                log.error("Objet with ID {} not found or archived", requete.getObjet().getId());
                return new ResponseEntity<>("{\"message\":\"Objet not found or archived\"}", HttpStatus.BAD_REQUEST);
            }
            requete.setObjet(objetOpt.get());
            if (requete.getClient() != null && requete.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository.findByIdAndArchiverFalse(requete.getClient().getId());
                if (clientOpt.isEmpty()) {
                    log.error("Client with ID {} not found or archived", requete.getClient().getId());
                    return new ResponseEntity<>("{\"message\":\"Client not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                requete.setClient(clientOpt.get());
            }
            if (requete.getGuichetier() != null && requete.getGuichetier().getId() != null) {
                Optional<UserInfo> guichetierOpt = userInfoRepository
                        .findByIdAndArchiverFalse(requete.getGuichetier().getId());
                if (guichetierOpt.isEmpty()) {
                    log.error("Guichetier with ID {} not found or archived", requete.getGuichetier().getId());
                    return new ResponseEntity<>("{\"message\":\"Guichetier not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                requete.setGuichetier(guichetierOpt.get());
            }
            if (requete.getTechnicien() != null && requete.getTechnicien().getId() != null) {
                Optional<UserInfo> technicienOpt = userInfoRepository
                        .findByIdAndArchiverFalse(requete.getTechnicien().getId());
                if (technicienOpt.isEmpty()) {
                    log.error("Technicien with ID {} not found or archived", requete.getTechnicien().getId());
                    return new ResponseEntity<>("{\"message\":\"Technicien not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                requete.setTechnicien(technicienOpt.get());
            }
            if (requete.getEtat() == null) {
                requete.setEtat(EtatRequete.NOUVEAU);
            }
            if (requete.getDate() == null) {
                requete.setDate(new Date());
            }
            requete.setArchiver(false);

            List<PieceJointe> piecesJointes = new ArrayList<>();
            if (files != null && files.length > 0) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        log.warn("Empty file received, skipping");
                        continue;
                    }
                    if (file.getSize() > MAX_FILE_SIZE) {
                        log.error("File {} exceeds maximum size of {} bytes", file.getOriginalFilename(),
                                MAX_FILE_SIZE);
                        return new ResponseEntity<>(
                                "{\"message\":\"File " + file.getOriginalFilename()
                                        + " exceeds maximum size of 10MB\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
                        log.error("Unsupported file type for {}: {}", file.getOriginalFilename(), contentType);
                        return new ResponseEntity<>(
                                "{\"message\":\"Unsupported file type for " + file.getOriginalFilename()
                                        + ". Allowed types: PDF, JPEG, PNG, DOC, DOCX\"}",
                                HttpStatus.BAD_REQUEST);
                    }
                    String originalFilename = file.getOriginalFilename();
                    String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
                    Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
                    Files.write(filePath, file.getBytes());

                    PieceJointe pieceJointe = new PieceJointe();
                    pieceJointe.setNomFichier(originalFilename);
                    pieceJointe.setTypeFichier(contentType);
                    pieceJointe.setCheminFichier(filePath.toString());
                    pieceJointe.setDateUpload(new Date());
                    pieceJointe.setRequete(requete);
                    pieceJointe.setArchiver(false);
                    piecesJointes.add(pieceJointe);
                }
            }
            requete.setPiecesJointes(piecesJointes);

            requeteRepository.save(requete);
            return new ResponseEntity<>("{\"message\":\"Requête ajoutée avec succès\"}", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding requete: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'ajout de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllRequetes() {
        try {
            List<Requete> requetes = requeteRepository.findByArchiverFalse();
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving all requetes: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des requêtes\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByClient(Long clientId) {
        try {
            List<Requete> requetes = requeteRepository.findByClientIdAndArchiverFalse(clientId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for client {}: {}", clientId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for client\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByGuichetier(Long guichetierId) {
        try {
            List<Requete> requetes = requeteRepository.findByGuichetierIdAndArchiverFalse(guichetierId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for guichetier {}: {}", guichetierId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for guichetier\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getRequetesByTechnicien(Long technicienId) {
        try {
            List<Requete> requetes = requeteRepository.findByTechnicienIdAndArchiverFalse(technicienId);
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving requetes for technicien {}: {}", technicienId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error retrieving requetes for technicien\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UserInfo findGuichetierWithLeastRequests() {
        List<UserInfo> guichetiers = userInfoRepository.findActiveGuichetiers();
        UserInfo selectedGuichetier = null;
        long minRequests = Long.MAX_VALUE;
        for (UserInfo guichetier : guichetiers) {
            long count = requeteRepository.countActiveRequetesForGuichetier(guichetier.getId());
            if (count < minRequests) {
                minRequests = count;
                selectedGuichetier = guichetier;
            }
        }
        return selectedGuichetier;
    }

    @Override
    public ResponseEntity<?> updateRequete(Long id, Requete updatedRequete) {
        try {
            Optional<Requete> existingRequeteOpt = requeteRepository.findByIdAndArchiverFalse(id);
            if (existingRequeteOpt.isEmpty()) {
                log.error("Requete with ID {} not found or archived", id);
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée ou archivée\"}", HttpStatus.NOT_FOUND);
            }
            Requete existingRequete = existingRequeteOpt.get();
            boolean isUpdated = false;
            if (updatedRequete.getObjet() != null && updatedRequete.getObjet().getId() != null) {
                Optional<Objet> objetOpt = objetRepository.findByIdAndArchiverFalse(updatedRequete.getObjet().getId());
                if (objetOpt.isEmpty()) {
                    log.error("Objet with ID {} not found or archived", updatedRequete.getObjet().getId());
                    return new ResponseEntity<>("{\"message\":\"Objet not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRequete.setObjet(objetOpt.get());
                isUpdated = true;
            }
            if (updatedRequete.getClient() != null && updatedRequete.getClient().getId() != null) {
                Optional<UserInfo> clientOpt = userInfoRepository
                        .findByIdAndArchiverFalse(updatedRequete.getClient().getId());
                if (clientOpt.isEmpty()) {
                    log.error("Client with ID {} not found or archived", updatedRequete.getClient().getId());
                    return new ResponseEntity<>("{\"message\":\"Client not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRequete.setClient(clientOpt.get());
                isUpdated = true;
            }
            if (updatedRequete.getGuichetier() != null && updatedRequete.getGuichetier().getId() != null) {
                Optional<UserInfo> guichetierOpt = userInfoRepository
                        .findByIdAndArchiverFalse(updatedRequete.getGuichetier().getId());
                if (guichetierOpt.isEmpty()) {
                    log.error("Guichetier with ID {} not found or archived", updatedRequete.getGuichetier().getId());
                    return new ResponseEntity<>("{\"message\":\"Guichetier not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRequete.setGuichetier(guichetierOpt.get());
                isUpdated = true;
            }
            if (updatedRequete.getTechnicien() != null && updatedRequete.getTechnicien().getId() != null) {
                Optional<UserInfo> technicienOpt = userInfoRepository
                        .findByIdAndArchiverFalse(updatedRequete.getTechnicien().getId());
                if (technicienOpt.isEmpty()) {
                    log.error("Technicien with ID {} not found or archived", updatedRequete.getTechnicien().getId());
                    return new ResponseEntity<>("{\"message\":\"Technicien not found or archived\"}",
                            HttpStatus.BAD_REQUEST);
                }
                existingRequete.setTechnicien(technicienOpt.get());
                isUpdated = true;
            }
            if (updatedRequete.getType() != null) {
                existingRequete.setType(updatedRequete.getType());
                isUpdated = true;
            }
            if (updatedRequete.getDescription() != null && !updatedRequete.getDescription().trim().isEmpty()) {
                existingRequete.setDescription(updatedRequete.getDescription());
                isUpdated = true;
            }
            if (updatedRequete.getEtat() != null) {
                existingRequete.setEtat(updatedRequete.getEtat());
                isUpdated = true;
            }
            if (updatedRequete.getNoteRetour() != null) {
                existingRequete.setNoteRetour(updatedRequete.getNoteRetour());
                isUpdated = true;
            }
            if (updatedRequete.getDate() != null) {
                existingRequete.setDate(updatedRequete.getDate());
                isUpdated = true;
            }
            if (updatedRequete.getDateTraitement() != null) {
                existingRequete.setDateTraitement(updatedRequete.getDateTraitement());
                isUpdated = true;
            }
            if (!isUpdated) {
                log.warn("No valid fields provided for update of requete ID {}", id);
                return new ResponseEntity<>("{\"message\":\"Aucun champ valide fourni pour la mise à jour\"}",
                        HttpStatus.BAD_REQUEST);
            }
            existingRequete.setArchiver(false);
            requeteRepository.save(existingRequete);
            return new ResponseEntity<>("{\"message\":\"Requête mise à jour avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating requete with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(
                    "{\"message\":\"Une erreur est survenue lors de la mise à jour de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> archiveRequete(Long id) {
        try {
            Optional<Requete> requeteOpt = requeteRepository.findByIdAndArchiverFalse(id);
            if (requeteOpt.isEmpty()) {
                log.error("Requete with ID {} not found or already archived", id);
                return new ResponseEntity<>("{\"message\":\"Requête non trouvée ou déjà archivée\"}",
                        HttpStatus.NOT_FOUND);
            }
            Requete requete = requeteOpt.get();
            requete.setArchiver(true);
            if (requete.getPiecesJointes() != null) {
                for (PieceJointe piece : requete.getPiecesJointes()) {
                    piece.setArchiver(true);
                }
            }
            requeteRepository.save(requete);
            return new ResponseEntity<>("{\"message\":\"Requête archivée avec succès\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error archiving requete with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de l'archivage de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAllArchivedRequetes() {
        try {
            List<Requete> requetes = requeteRepository.findByArchiverTrue();
            return new ResponseEntity<>(requetes, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving archived requetes: {}", e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors de la récupération des requêtes archivées\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> unarchiveRequete(Long id) {
        try {
            Optional<Requete> requeteOpt = requeteRepository.findById(id);
            if (requeteOpt.isEmpty()) {
                return new ResponseEntity<>("{\"message\":\"Requete not found\"}", HttpStatus.NOT_FOUND);
            }
            Requete requete = requeteOpt.get();
            if (!requete.isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Requete is not archived\"}", HttpStatus.BAD_REQUEST);
            }
            if (requete.getObjet().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive requete: Objet is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (requete.getClient() != null && requete.getClient().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive requete: Client is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (requete.getGuichetier() != null && requete.getGuichetier().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive requete: Guichetier is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            if (requete.getTechnicien() != null && requete.getTechnicien().isArchiver()) {
                return new ResponseEntity<>("{\"message\":\"Cannot unarchive requete: Technicien is archived\"}",
                        HttpStatus.BAD_REQUEST);
            }
            requete.setArchiver(false);
            if (requete.getPiecesJointes() != null) {
                for (PieceJointe piece : requete.getPiecesJointes()) {
                    piece.setArchiver(false);
                }
            }
            requeteRepository.save(requete);
            return new ResponseEntity<>("{\"message\":\"Requete unarchived successfully\"}", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error unarchiving requete with ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Erreur lors du désarchivage de la requête\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> downloadPieceJointe(Long pieceJointeId) {
        try {
            Optional<PieceJointe> pieceJointeOpt = pieceJointeRepository.findByIdAndArchiverFalse(pieceJointeId);
            if (pieceJointeOpt.isEmpty()) {
                log.error("PieceJointe with ID {} not found or archived", pieceJointeId);
                return new ResponseEntity<>("{\"message\":\"Piece jointe not found or archived\"}",
                        HttpStatus.NOT_FOUND);
            }
            PieceJointe pieceJointe = pieceJointeOpt.get();
            File file = new File(pieceJointe.getCheminFichier());
            if (!file.exists()) {
                log.error("File not found on server: {}", pieceJointe.getCheminFichier());
                return new ResponseEntity<>("{\"message\":\"File not found on server\"}", HttpStatus.NOT_FOUND);
            }
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pieceJointe.getNomFichier());
            headers.setContentType(MediaType.parseMediaType(pieceJointe.getTypeFichier()));
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error downloading piece jointe with ID {}: {}", pieceJointeId, e.getMessage(), e);
            return new ResponseEntity<>("{\"message\":\"Error downloading piece jointe\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
package com.edws.gov.repo;

import com.edws.gov.entity.DisasterDocument;
import com.edws.gov.enums.DocumentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DisasterDocumentRepository extends MongoRepository<DisasterDocument, String> {

    List<DisasterDocument> findByStatus(DocumentStatus status);

    List<DisasterDocument> findByType(String type);

    List<DisasterDocument> findByCreatedBy(String createdBy);

    List<DisasterDocument> findByStatusOrderByCreatedAtDesc(DocumentStatus status);
}

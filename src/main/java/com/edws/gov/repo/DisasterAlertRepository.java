package com.edws.gov.repo;

import com.edws.gov.entity.DisasterAlert;
import com.edws.gov.enums.AlertStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DisasterAlertRepository extends MongoRepository<DisasterAlert, String> {

    boolean existsByReferenceCode(String referenceCode);

    long countByStatus(AlertStatus status);
}

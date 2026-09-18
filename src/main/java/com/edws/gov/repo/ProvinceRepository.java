package com.edws.gov.repo;

import com.edws.gov.entity.Province;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProvinceRepository extends MongoRepository<Province, String> {

    Optional<Province> findByName(String name);
}

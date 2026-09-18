package com.edws.gov.repo;

import com.edws.gov.entity.District;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends MongoRepository<District, String> {

    Optional<District> findByName(String name);

    List<District> findByProvinceId(String provinceId);
}

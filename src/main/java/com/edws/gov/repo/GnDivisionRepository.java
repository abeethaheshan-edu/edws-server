package com.edws.gov.repo;

import com.edws.gov.entity.GnDivision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface GnDivisionRepository extends MongoRepository<GnDivision, String> {

    Optional<GnDivision> findByCode(String code);

    List<GnDivision> findByDistrictId(String districtId);

    @Query("{ boundary: { $geoIntersects: { $geometry: { type: 'Point', coordinates: [ ?0, ?1 ] } } } }")
    Optional<GnDivision> findContaining(double lng, double lat);
}

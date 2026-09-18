package com.edws.gov.entity;

import com.edws.gov.dto.user.AddressDto;
import com.edws.gov.dto.user.PropertyRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

//@Document(collection = "properties")
//@Getter
//@Setter
//public class Property extends BaseDocument {
//    private String ownerId;
//    private String label;
//    private String houseNo;
//    private String street;
//    private String town;
//    private String gnDivisionId;
//    private String districtId;
//    private String provinceId;
//    private GeoJsonPoint location;
//
//    public PropertyRequest toPropertyRequest() {
//
//        Double latitude = null;
//        Double longitude = null;
//
//        if (location != null) {
//            latitude = location.getY();
//            longitude = location.getX();
//        }
//
//        return new PropertyRequest(
//                label,
//                new AddressDto(
//                        houseNo,
//                        street,
//                        null,
//                        null,          // zipCode
//                        town,
//                        latitude,
//                        longitude
//                )
//        );
//    }
//}

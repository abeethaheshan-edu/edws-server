package com.edws.gov.entity;

import com.edws.gov.dto.user.AddressDto;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "address")
public class Address extends BaseDocument{
      private  String houseHolderId;
      private  String gnDivision;
      private  String houseName;
      private  String houseNo;
      private  String streetAddress1;
      private  String streetAddress2;
      private  String zipCode;
      private  String city;
      private  boolean isPrimary;

      @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
      private GeoJsonPoint location;

      public  Address(){

      }
      public Address(AddressDto dto) {
            this.houseName = dto.houseName();
            this.houseNo = dto.houseNo();
            this.streetAddress1 = dto.streetAddress1();
            this.streetAddress2 = dto.streetAddress2();
            this.zipCode = dto.zipCode();
            this.city = dto.city();
            this.gnDivision = dto.gnDivision();

            if (dto.hasCoordinates()) {
                this.location = new GeoJsonPoint(dto.longitude(), dto.latitude());
            }
      }

}

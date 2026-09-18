package com.edws.gov.entity;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseDocument {
    private String code;
    private String name;
    private String description;
}

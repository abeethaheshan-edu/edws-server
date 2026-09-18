package com.edws.gov.entity;

import com.edws.gov.enums.ApprovalAction;
import com.edws.gov.enums.ApproverLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class DocumentApprover {
    private int order;
    private String approverId;
    private String name;
    private String position;
    private ApproverLevel level;
    private ApprovalAction action = ApprovalAction.PENDING;
    private String signatureUrl;
    private String comment;
    private Instant signedAt;
}

package com.edws.gov.repo;

/*
 * DISABLED - does not compile as written.
 *
 * This interface referenced an `Approver` entity that has no source file in the project
 * (only a stale Approver.class was left behind under build/). As a result the module could
 * not be compiled at all, which is why it is commented out here rather than left broken.
 *
 * The nearest existing type is `com.edws.gov.entity.DocumentApprover`, but that is an
 * embedded value object inside DisasterDocument, not a @Document collection, so it cannot
 * back a MongoRepository. Restoring this needs a decision that belongs to you: either
 * recreate the standalone Approver entity, or drop this repository and read approvers
 * through DisasterDocumentRepository.
 *
 * Original content:
 *
 * public interface ApproverRepository extends MongoRepository<Approver, String> {
 *     Optional<Approver> findByEmail(String email);
 *     List<Approver> findByLevel(ApproverLevel level);
 *     List<Approver> findByActiveTrue();
 *     List<Approver> findByCreatedBy(String createdBy);
 * }
 */

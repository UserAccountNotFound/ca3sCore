package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the ProtectedContent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProtectedContentRepository extends JpaRepository<ProtectedContent, Long> {

	  @Query(name = "ProtectedContent.findByCertificateId")
	  List<ProtectedContent> findByCertificateId(@Param("certId") Long certId);

	  @Query(name = "ProtectedContent.findByCSRId")
	  List<ProtectedContent> findByCSRId(@Param("csrId") Long csrId);

	  @Query(name = "ProtectedContent.findByTypeRelationId")
	  List<ProtectedContent> findByTypeRelationId(@Param("type") ProtectedContentType type, @Param("relationType") ContentRelationType relationType, @Param("id") Long id);
	
}

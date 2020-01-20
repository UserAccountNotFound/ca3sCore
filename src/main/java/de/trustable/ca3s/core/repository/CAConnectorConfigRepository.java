package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CAConnectorConfig entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CAConnectorConfigRepository extends JpaRepository<CAConnectorConfig, Long> {

}

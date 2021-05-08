package de.trustable.ca3s.core.repository;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import de.trustable.ca3s.core.service.util.CertificateSelectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.dto.CertificateView;


@Service
public class CSRViewRepository {

	@Autowired
    private EntityManager entityManager;

    @Autowired
    private CertificateSelectionUtil certificateSelectionAttributeList;

    public Page<CSRView> findSelection(Map<String, String[]> parameterMap){

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		return CSRSpecifications.handleQueryParamsCertificateView(entityManager,
				cb,
				parameterMap,
            certificateSelectionAttributeList.getCertificateSelectionAttributes());

	}

}

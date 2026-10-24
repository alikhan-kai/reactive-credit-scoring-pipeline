package kz.kaspi.credit.agreement.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import kz.kaspi.credit.agreement.domain.CreditAgreement;
import reactor.core.publisher.Flux;

@Repository
public interface CreditAgreementRepository extends R2dbcRepository<CreditAgreement, UUID> {
    Flux<CreditAgreement> findAllByIin(String iin);
}

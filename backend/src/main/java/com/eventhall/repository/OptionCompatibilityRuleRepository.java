package com.eventhall.repository;

import com.eventhall.entity.OptionCompatibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionCompatibilityRuleRepository extends JpaRepository<OptionCompatibilityRule, Long> {

    List<OptionCompatibilityRule> findByParentOption_IdAndActiveTrue(Long parentOptionId);

    boolean existsByParentOption_IdAndChildOption_IdAndActiveTrue(Long parentOptionId, Long childOptionId);

    /** Returns all active rules; used to build the full compatibility map for the customer builder. */
    List<OptionCompatibilityRule> findAllByActiveTrue();

    /** Returns all rules (active or not) for a given parent, used when replacing the set. */
    List<OptionCompatibilityRule> findAllByParentOption_Id(Long parentOptionId);
}

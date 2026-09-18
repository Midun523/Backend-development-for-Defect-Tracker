package com.defecttracker.repository;

import com.defecttracker.entity.UserExtraRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExtraRuleRepository extends JpaRepository<UserExtraRule, Long> {
    List<UserExtraRule> findByUserId(Long userId);
    Optional<UserExtraRule> findByUserIdAndRuleKey(Long userId, String ruleKey);
}

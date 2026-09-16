package com.hcmute.springboot.repository;

import com.hcmute.springboot.entity.OtpPurpose;
import com.hcmute.springboot.entity.OtpToken;
import com.hcmute.springboot.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Integer> {

    @Query("SELECT t FROM OtpToken t WHERE t.user = :user AND t.purpose = :purpose AND t.used = false AND t.expiresAt > :now AND t.attempts < 5 ORDER BY t.createdAt DESC")
    List<OtpToken> findValidTokens(@Param("user") User user, @Param("purpose") OtpPurpose purpose, @Param("now") Timestamp now, Pageable pageable);

    @Modifying
    @Query("UPDATE OtpToken t SET t.used = true WHERE t.user = :user AND t.purpose = :purpose AND t.used = false")
    void invalidateExistingByUserAndPurpose(@Param("user") User user, @Param("purpose") OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpToken t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}

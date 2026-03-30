package com.callyzer.repository;

import com.callyzer.model.CallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    List<CallLog> findByDeviceIdOrderByCallTimestampDesc(String deviceId);

    Page<CallLog> findAllByOrderByCallTimestampDesc(Pageable pageable);

    long countByCallType(CallLog.CallType callType);

    @Query("SELECT DISTINCT c.deviceId FROM CallLog c")
    List<String> findDistinctDeviceIds();

    @Query("SELECT c.deviceId, COUNT(c) FROM CallLog c GROUP BY c.deviceId")
    List<Object[]> countByDevice();

    boolean existsByDeviceIdAndPhoneNumberAndCallTimestamp(String deviceId, String phoneNumber, Long callTimestamp);

    // === NEW: Efficient aggregate queries ===

    // Average duration of all calls (replaces findAll().stream() which loads all rows into memory)
    @Query("SELECT COALESCE(AVG(c.duration), 0) FROM CallLog c WHERE c.duration IS NOT NULL")
    double findAverageDuration();

    // Total talk time today (sum of all durations for calls today)
    @Query("SELECT COALESCE(SUM(c.duration), 0) FROM CallLog c WHERE c.callTimestamp >= :startOfDay")
    long findTotalDurationSince(@Param("startOfDay") long startOfDay);

    // Count of calls today
    @Query("SELECT COUNT(c) FROM CallLog c WHERE c.callTimestamp >= :startOfDay")
    long countCallsSince(@Param("startOfDay") long startOfDay);

    // Today's incoming calls count
    @Query("SELECT COUNT(c) FROM CallLog c WHERE c.callTimestamp >= :startOfDay AND c.callType = :callType")
    long countCallsSinceByType(@Param("startOfDay") long startOfDay, @Param("callType") CallLog.CallType callType);
}

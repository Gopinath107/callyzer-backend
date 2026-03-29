package com.callyzer.repository;

import com.callyzer.model.CallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}

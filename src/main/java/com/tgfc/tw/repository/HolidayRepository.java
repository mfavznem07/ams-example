package com.tgfc.tw.repository;

import com.tgfc.tw.entity.po.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    List<Holiday> findAllByDateBetween(Date startDate, Date endDate);

    Optional<Holiday> findByDate(Date date);
}

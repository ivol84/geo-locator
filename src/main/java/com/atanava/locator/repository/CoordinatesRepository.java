package com.atanava.locator.repository;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface CoordinatesRepository extends JpaRepository<Point, PointId> {
}

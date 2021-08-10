package com.atanava.locator.repository;

import com.atanava.locator.model.Point;
import com.atanava.locator.model.PointId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;

@Transactional(readOnly = true)
public interface PointRepository extends JpaRepository<Point, PointId> {

	Set<Point> findByPointIdIn(Collection<@NotNull PointId> pointId);
}

package com.atanava.locator.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Point {

	@EmbeddedId
	@NotNull
	private PointId pointId;

	@NotNull
	@NotEmpty
	@Column(name = "osm_id")
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> osmIds;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point that = (Point) o;

		return Objects.equals(pointId, that.pointId);
	}

	@Override
	public int hashCode() {
		return pointId != null ? pointId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return pointId.toString();
	}
}

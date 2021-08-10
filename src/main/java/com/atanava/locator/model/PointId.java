package com.atanava.locator.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class PointId implements Serializable {

	@Column(name = "lat")
	private double lat;

	@Column(name = "lon")
	private double lon;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PointId that = (PointId) o;

		if (Double.compare(that.lat, lat) != 0) return false;
		return Double.compare(that.lon, lon) == 0;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "Point{" +
				"latitude=" + lat +
				", longitude=" + lon +
				'}';
	}
}

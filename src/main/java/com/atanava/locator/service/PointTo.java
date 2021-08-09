package com.atanava.locator.service;

import com.atanava.locator.model.PointId;
import lombok.Value;

import java.util.Set;

@Value
public class PointTo {

	PointId pointId;
	Set<String> osmIds;
	String type;
}

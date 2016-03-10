package com.ozgen.server.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = { "/protected/rest/location" })
@CrossOrigin(origins = "*")
public class LocationController {

	List<Location> locations = new ArrayList<Location>();

	@RequestMapping(value = { "/{locationId}" }, method = { RequestMethod.GET })
	public Location get(@PathVariable("locationId") String locationId) {

		Location entity = new Location("999", "amsterdam");
		for (int i = 0; i < locations.size(); i++) {
			if (locations.get(i).getId().equals(locationId))
				return locations.get(i);
		}
		return entity;

	}

	@RequestMapping(method = { RequestMethod.POST })
	public void add(Principal principal, HttpServletRequest request, HttpServletResponse response,
			@RequestBody Location data) {

		locations.add(data);
		response.setHeader("Location", request.getRequestURI() + data.getId());
	}

}

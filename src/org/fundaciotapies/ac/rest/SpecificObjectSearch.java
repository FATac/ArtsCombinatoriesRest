package org.fundaciotapies.ac.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/specific
 * <br>
 * Global search filtering by field-value pair. Results can optionally be filtered by class (and its subclasses)<br>
 * Param:<br>
 * - f: field name<br>
 * - v: value<br>
 * - c: class name<br>
 * Return: JSON structured list of objects (which are field-value pairs list)
 */
@Path("/specific")
public class SpecificObjectSearch {

	@GET
	@Produces("application/json")
	public String specificObjectSearch(@QueryParam("f") String field,  @QueryParam("v") String value, @QueryParam("c") String className) {
		List<String> result = new Request().specificObjectSearch(field, value, className);
		return new Gson().toJson(result);
	}
}

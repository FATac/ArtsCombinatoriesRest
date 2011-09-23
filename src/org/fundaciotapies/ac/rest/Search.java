package org.fundaciotapies.ac.rest;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Request;
import org.fundaciotapies.ac.model.support.CustomMap;

import com.google.gson.Gson;

/**
 * Call: http://{host:port}/search
 * <br>
 * Basic global search which calls a string based query for all triplet contents. Results can optionally be filtered by given class (and its subclasses)<br>
 * Param:<br>
 * - s: search word<br>
 * - c: class name (optional)<br>
 * - u: user id fot legal restrictions purposes (optional)<br>
 */
@Path("/search")
public class Search {

	@GET
	@Produces("application/json")
	public String search(@QueryParam("s") String word, @QueryParam("c") String className, @QueryParam("u") String uid) {
		Map<String, CustomMap> result = new Request().search(word, className, uid);
		return new Gson().toJson(result);
	}
}

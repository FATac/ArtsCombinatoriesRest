package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Upload;

import com.google.gson.Gson;

@Path("deleteObject")
public class DeleteObject {

	@GET
	@Produces("application/json")
	public String deleteObject(@QueryParam("id") String id) {
		String result = new Upload().deleteObject(id);
		return new Gson().toJson(result);
	}
}

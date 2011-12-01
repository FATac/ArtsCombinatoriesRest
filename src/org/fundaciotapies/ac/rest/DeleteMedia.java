package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fundaciotapies.ac.model.Upload;

@Path("/media/{id}/delete")
public class DeleteMedia {

	@GET
	@Produces("application/json")
	public String deleteMedia(@PathParam("id") String id) {
		return new Upload().removeMediaFile(id);
	}

}

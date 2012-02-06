package org.fundaciotapies.ac.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.support.Template;
import org.fundaciotapies.ac.view.ViewGenerator;

import com.google.gson.Gson;

@Path("/resource/{id}/view")
public class GetObjectView {

	@GET
	@Produces("application/json")
	public String getObjectView(@PathParam("id") String id, @QueryParam("section") String section, @QueryParam("uid") String uid, @QueryParam("lang") String lang) {
		String[] ids = id.split(",");
		if ("".equals(uid)) uid = null;
		if (ids.length==1) {
			Template result = new ViewGenerator().getObjectView(id, section, uid, lang);
			if (result==null) return new Gson().toJson("Error: Object class has no template");
			return new Gson().toJson(result);
		} else {
			List<Template> result = new ArrayList<Template>();
			for (String oid : ids) {
				Template tmp = new ViewGenerator().getObjectView(oid, section, uid, lang);
				result.add(tmp);
			}
			return new Gson().toJson(result);
		}
	}

}

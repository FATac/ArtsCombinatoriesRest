package org.fundaciotapies.ac.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.fundaciotapies.ac.model.Upload;


import com.google.gson.Gson;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

@Path("/reset")
public class Reset {
	
	@GET
	@Produces("application/json")
	public String clear(@QueryParam("confirm") String confirm, @QueryParam("option") String opt) {
		if ("ontology".equals(opt)) {
			try {
				new Upload().reset(false);
			} catch (Exception e) {
				return new Gson().toJson("error");
			}
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy kk:mm");
			String dateStr = sdf.format(Calendar.getInstance().getTime());
			
			if (dateStr.equals(confirm)) {
				try {
					new Upload().reset(true);
				} catch (Exception e) {
					return new Gson().toJson("error");
				}
			} else {
				return new Gson().toJson("Wrong confirmation date. Server date is " + dateStr);
			}
		}
		
		return new Gson().toJson("success");
	}

}

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
	public String clear(@QueryParam("confirm") String confirm) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy kk:mm");
		
		if (sdf.format(Calendar.getInstance().getTime()).equals(confirm)) {
			try {
				new Upload().reset();
			} catch (Exception e) {
				return new Gson().toJson("error");
			}
		}
		
		return new Gson().toJson("success");
	}

}

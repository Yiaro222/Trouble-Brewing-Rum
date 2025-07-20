
package com.RumRunning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.runelite.client.RuneLite;



/* This class is perhaps oddly structured. I wanted to keep it dynamic to
 * avoid any issues with the user changing account, but I'm not sure the
 * (however small) performance cost to re-creating io buffers each function-call
 * is worth it. */

public class Cache
{
	private Gson gsonHandle = null;
	
	private final String directory = RuneLite.RUNELITE_DIR + "/trouble-brewing-rum/";
	
	public static class Data
	{
		List<Integer> collection_log_ids = new ArrayList<>();
		int           eight_count        = 0;
	}
	public static Data data = new Data();
	
	public void
	write(String username)
	{
		if (username == null || username.isEmpty())
		{
			return;
		}
		
		if (gsonHandle == null)
		{
			gsonHandle = new GsonBuilder().setPrettyPrinting().create();
		}
		
		try (FileWriter outputStream = new FileWriter(directory + username))
		{
			gsonHandle.toJson(data, outputStream);
		}
		catch (IOException e) {  }
	}
	
	public void
	read(String username)
	{
		if (username == null || username.isEmpty())
		{
			return;
		}
		
		if (gsonHandle == null)
		{
			gsonHandle = new GsonBuilder().setPrettyPrinting().create();
		}
		
		try (FileReader inputStream = new FileReader(directory + username))
		{
			data = gsonHandle.fromJson(inputStream, Data.class);
		}
		catch (IOException e) {  }
	}
	
	public void
	createDirectory()
	{
		File dir;
		
		dir = new File(directory);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
	}
	
}




/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class VideoController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	
	Map<Long, Video> videoRepo = new HashMap<>();
	
	@GetMapping("/video")
	public List<Video> getAllVideos()
	{
		return new ArrayList<>(this.videoRepo.values());
	}
	
	@PostMapping("/video")
	public Video addVideo(@RequestBody Video v)
	{
		
		v.setId(this.videoRepo.keySet().size()+1L);
		v.setDataUrl(getDataUrl(v.getId()));
		this.videoRepo.put(v.getId(), v);
		return v;
		
	}
	
	
	@PostMapping("/video/{id}/data")
	public VideoStatus postVideoData(@PathVariable("id") long id, 
			@RequestParam("data") MultipartFile videoData,
			HttpServletResponse response, HttpServletRequest request) throws IOException
	{
		if(this.videoRepo.get(id)!=null)
		{
			Video video = this.videoRepo.get(id);
			VideoFileManager.get().saveVideoData(video, videoData.getInputStream());
			response.setStatus(200);
			return new VideoStatus((VideoStatus.VideoState.READY));
		}
		response.setStatus(404);
		return null;
	}
	
	
	
	@GetMapping("/video/{id}/data")
	public void getVideoData(@PathVariable("id") long id, 
			HttpServletResponse response) throws IOException
	{
		if(videoRepo.get(id)!=null)
		{
			Video v = videoRepo.get(id);
			if(VideoFileManager.get().hasVideoData(v))
			{
				VideoFileManager.get().copyVideoData(v, response.getOutputStream());
				response.setStatus(200);
			}
			else
			{
				response.setStatus(404);
			}
			return;
		}
		response.setStatus(404);
		
	}

	
	
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://"+request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}

	
}

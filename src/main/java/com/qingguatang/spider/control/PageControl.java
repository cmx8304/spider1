package com.qingguatang.spider.control;

import com.qingguatang.spider.dataobject.PlayListDO;
import com.qingguatang.spider.dataobject.SongDO;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author cmx 2018/8/3.
 * @version 1.0
 */
@Controller
public class PageControl {
    private static final Logger logger = LoggerFactory.getLogger(PageControl.class);

    private static OkHttpClient client = new OkHttpClient();

    @Autowired
    private PlayListControl playListControl;

    @RequestMapping(value = "/admin")
    public String admin(){
        return "admin";
    }

    @RequestMapping(value = "/spider")
    public String spider(String url){
        if(StringUtils.isBlank(url)){
            return "redirect:admin";
        }

        Builder builder = new Builder();
        builder.add("url",url);

        Request request = new Request.Builder().url("http://localhost:8088").post(builder.build()).build();

        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            logger.error(body);
        }catch (IOException e){
            logger.error("",e);
        }


        return "redirect:admin";
    }

    @RequestMapping(value = "/home")
    public String home(String playListId, ModelMap modelMap){
        List<PlayListDO> playListDOS = playListControl.query();
        if(StringUtils.isBlank(playListId)){
            playListId = playListDOS.get(0).getId();
        }

        List<SongDO> songDOS = playListControl.querySongs(playListId);

        modelMap.addAttribute("playlists",playListDOS);
        modelMap.addAttribute("playListId",playListId);
        modelMap.addAttribute("songs",songDOS);
        return "home";
    }



    @RequestMapping(value= "/music/{songId}.mp3")
    @ResponseBody
    public ResponseEntity music(@PathVariable("songId") String songId){

        File file = new File("musicdata",songId + ".mp3");
        ResponseEntity responseEntity = null;
        responseEntity = new ResponseEntity(new FileSystemResource(file),HttpStatus.OK);
        System.out.println("responseEntity" + responseEntity);
        return responseEntity;

    }
}

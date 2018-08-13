package com.qingguatang.spider.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingguatang.spider.service.DownloadService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * @author cmx 2018/7/30.
 * @version 1.0
 */
@Component
public class DownloadServiceImpl implements DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);

    private static OkHttpClient client = new OkHttpClient();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Executor executor = Executors.newFixedThreadPool(10);
    /**
     * 初始化任务
     * Scheduled 循环执行该方法，15s后执行，30s再次执行
     * @PostConstruct 让方法在应用启动过程中就开始执行
     */
    //使用时打开
    /*@Scheduled(initialDelay = 15000, fixedRate = 30000)
    @PostConstruct
    public void init(){
        File root = new File("data","music");
        if(!root.exists()){
            return;
        }

        //读取所有子文件
        for (File file : root.listFiles()){
            try {
                handlerDownload(file);
            }catch (Exception e){
                logger.error("the music file:" + file.getName() + "download error", e);
            }
        }
    }*/

    private void handlerDownload(File file) throws IOException{
        //使用jackson解析 json
        //json当成map处理
        Map map = objectMapper.readValue(file,Map.class);
        Integer id = (Integer)map.get("id");
        String url = (String) map.get("url");
        String fileName = id + ".mp3";

        if(StringUtils.isBlank(url)){
            return;
        }

        File mp3 = new File(getRoot(),fileName);
        if(mp3.exists()){
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                download(fileName,url);
            }
        });
    }
    @Override
    public void download(String fileName, String url) {

        //创建一个okhttp的request对象
        Request request = new Request.Builder().url(url).build();

        try{
            //获得一个response对象
            Response response = client.newCall(request).execute();

            //创建文件写入流（file对象（根目录，文件名））
            FileOutputStream outputStream = new FileOutputStream(new File(getRoot(),fileName));

            //自动写入文件，IOUtils由commons-io依赖库提供
            IOUtils.write(response.body().bytes(),outputStream);

        }catch (Exception e){
            logger.error("download" + fileName + "error:" , e);
        }

    }

    /**
     * 获取音乐文件的文件夹目录
     * @return
     */
    private File getRoot(){
        File root = new File("musicdata");
        if(!root.exists()){
            root.mkdir();
        }
        return  root;
    }
}

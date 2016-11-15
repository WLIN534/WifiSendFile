package com.zl.wifi.wifisendfile.httpserver;

import com.zl.wifi.wifisendfile.Config.ConstValue;
import com.zl.wifi.wifisendfile.Config.HtmlConst;
import com.zl.wifi.wifisendfile.utils.FileAccessUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 文件服务
 * Created by zhanglin on 2016/7/12.
 */
public class SimpleFileServer extends  NanoHTTPD{


    public SimpleFileServer(int port) {
        super(port);
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parms,
                          Map<String, String> files) {
        if (Method.GET.equals(method)) {
            return new Response(HtmlConst.HTML_STRING);
        } else {
            for (String s : files.keySet()) {
                try {
                    FileInputStream fis = new FileInputStream(files.get(s));

                    FileOutputStream fos = new FileOutputStream(
                            FileAccessUtil.getFile(ConstValue.BASE_DIR + "/"
                                    + parms.get("file")));
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int byteRead = fis.read(buffer);
                        if (byteRead == -1) {
                            break;
                        }
                        fos.write(buffer, 0, byteRead);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new Response(HtmlConst.HTML_STRING);
        }
    }

}

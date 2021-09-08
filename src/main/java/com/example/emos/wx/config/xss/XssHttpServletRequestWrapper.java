package com.example.emos.wx.config.xss;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Classname XssHttpServletRequestWrapper
 * @Description TODO
 * @Date 2021/7/24 15:41
 * @Created by GZK0329
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StrUtil.isEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (!StrUtil.hasEmpty(values)) {
            int i = 0;
            for (String value : values) {
                value = HtmlUtil.filter(value);
                values[i++] = value;
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (!parameters.isEmpty()) {
            for (String s : parameters.keySet()) {
                String[] values = parameters.get(s);
                if (!StrUtil.hasEmpty(values)) {
                    for (int i = 0; i < values.length; i++) {
                        String value = HtmlUtil.filter(values[i]);
                        values[i] = value;
                    }
                    map.put(s, values);
                }
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String header = super.getHeader(name);
        if (!StrUtil.hasEmpty(header)) {
            header = HtmlUtil.filter(header);
        }
        return header;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();

        while (line != null) {
            body.append(line);
            line = buffer.readLine();
        }

        buffer.close();
        reader.close();
        in.close();

        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        if (!map.isEmpty()) {
            for (String s : map.keySet()) {
                String value =  map.get(s).toString();
                if (!StrUtil.hasEmpty(value)) {
                    value = HtmlUtil.filter(value);
                    resultMap.put(s, value);
                }
            }
        }
        String jsonStr = JSONUtil.toJsonStr(resultMap);
        ByteArrayInputStream byteArray = new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArray.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}

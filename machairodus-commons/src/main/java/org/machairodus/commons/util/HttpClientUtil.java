/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.machairodus.commons.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.machairodus.commons.exception.HttpClientException;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class HttpClientUtil {
	private static Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);
	
	public static <T> T post(String uri, TypeReference<T> type) {
		String result = post(uri, Collections.emptyMap());
		return JSON.parseObject(result, type);
	}
	
	public static <T> T post(String uri, Map<String, Object> params, TypeReference<T> type) {
		LOG.debug("Remote Invoke: " + uri);
		String result = post(uri, params);
		return JSON.parseObject(result, type);
	}
	
	public static String post(String uri) {
		return post(uri, Collections.emptyMap());
	}
	
	public static String post(String uri, Map<String, Object> params) {
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
		CloseableHttpClient httpClient = HttpClients.createDefault();  
        HttpPost httpPost = new HttpPost(uri);  
        CloseableHttpResponse response = null;  
  
        try {  
            httpPost.setConfig(requestConfig);  
            if(!CollectionUtils.isEmpty(params)) {
	            List<NameValuePair> pairList = new ArrayList<>(params.size());  
	            for (Map.Entry<String, Object> entry : params.entrySet()) {  
	                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());  
	                pairList.add(pair);  
	            }  
	            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));  
            }
            
            response = httpClient.execute(httpPost);  
            LOG.debug(response.toString());  
            
            HttpEntity entity = response.getEntity();  
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {  
            throw new HttpClientException("Http Post Error: " + e.getMessage());
            
        } finally {  
            if (response != null) {  
                try {  
                    EntityUtils.consume(response.getEntity());  
                } catch (IOException e) {  
                	throw new HttpClientException("EntityUtils.consume Error: " + e.getMessage());
                }  
            }  
        }  
        
	}
}

package util;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class HttpClientUtil {
    private static final CloseableHttpClient httpClient;
    public static final int DEFAULT_TIMEOUT = 3000;

    static {
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).setSoTimeout(5000)
            .build();
        httpClient = HttpClientBuilder.create().setMaxConnTotal(200).setMaxConnPerRoute(20)
            .setDefaultSocketConfig(socketConfig).setSSLSocketFactory(createSSLConnSocketFactory()).build();

    }

    /**
     * @param url         url
     * @param proxy       代理
     * @param cookieStore cookie
     * @return
     * @throws Exception
     */
    public static String get(String url, HttpHost proxy, CookieStore cookieStore) throws Exception {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader("User-Agent", UserAgentUtil.getPcUserAgent()));
        return get(url, headerList, proxy, cookieStore);
    }

    /**
     * @param url
     * @param headerList
     * @param proxy
     * @param cookieStore
     * @return
     * @throws Exception
     */
    public static String get(String url, List<Header> headerList, HttpHost proxy, CookieStore cookieStore)
        throws Exception {
        HttpGet httpGet = new HttpGet(url);
        if (headerList != null)
            httpGet.setHeaders(headerList.toArray(new Header[0]));
        ResponseHandler<String> responseHandler = getResponseHandler();
        return execute(httpGet, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, proxy, cookieStore, responseHandler);
    }

    /**
     * @param url
     * @param headerList
     * @param proxy
     * @param cookieStore
     * @param responseHandler
     * @return
     * @throws Exception
     */
    public static <T> T get(String url, List<Header> headerList, HttpHost proxy, CookieStore cookieStore,
                            ResponseHandler<? extends T> responseHandler) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        if (headerList != null)
            httpGet.setHeaders(headerList.toArray(new Header[0]));
        return execute(httpGet, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, proxy, cookieStore, responseHandler);
    }

    public static String post(String url, List<NameValuePair> nameValuePairList, HttpHost proxy,
                              CookieStore cookieStore) throws Exception {
        List<Header> headerList = new ArrayList<Header>();
        headerList.add(new BasicHeader("User-Agent", UserAgentUtil.getPcUserAgent()));
        return post(url, "utf-8", headerList, nameValuePairList, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, proxy, cookieStore);
    }

    public static String post(String url, String charset, List<Header> headerList,
                              List<NameValuePair> nameValuePairList, int connTimeOut, int waitTimeOut, HttpHost proxy,
                              CookieStore cookieStore) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (headerList != null)
            httpPost.setHeaders(headerList.toArray(new Header[0]));
        if (nameValuePairList != null)
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, charset));
        ResponseHandler<String> responseHandler = getResponseHandler();
        return execute(httpPost, connTimeOut, waitTimeOut, proxy, cookieStore, responseHandler);
    }

    /**
     * @param url
     * @param headerList
     * @param nameValuePairList
     * @param proxy
     * @param cookieStore
     * @param responseHandler
     * @return
     * @throws Exception
     */
    public static <T> T post(String url, List<Header> headerList, List<NameValuePair> nameValuePairList, HttpHost proxy,
                             CookieStore cookieStore, ResponseHandler<? extends T> responseHandler) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (headerList != null)
            httpPost.setHeaders(headerList.toArray(new Header[0]));
        if (nameValuePairList != null)
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
        return execute(httpPost, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, proxy, cookieStore, responseHandler);
    }

    public static <T> T execute(HttpUriRequest request, int connTimeOut, int waitTimeOut, HttpHost proxy,
                                CookieStore cookieStore, ResponseHandler<? extends T> responseHandler) throws Exception {
        CloseableHttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connTimeOut)
                .setConnectTimeout(connTimeOut).setSocketTimeout(waitTimeOut).setProxy(proxy).build();
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setRequestConfig(requestConfig);
            if (cookieStore != null)
                localContext.setCookieStore(cookieStore);
            return httpClient.execute(request, responseHandler, localContext);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static ResponseHandler<String> getResponseHandler() {
        return new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
    }

    public static ResponseHandler<byte[]> getByteArrayResponseHandler() {
        return new ResponseHandler<byte[]>() {

            @Override
            public byte[] handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
    }

    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }
}

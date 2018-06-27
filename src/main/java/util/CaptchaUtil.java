package util;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import ocr.ImageUtil;
import ocr.Recognizer;
import ocr.WanderConfig;
import ocr.ZimuService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class CaptchaUtil {
    private static Logger logger = Logger.getLogger(CaptchaUtil.class);
    // 验证码包括4个字符
    private static final String CODE_TYPE = "3040";
    // 是否将图片转换为JPG格式，转换后识别率提升
    private static final String IF_CONVERT_JPG = "1";
    // 验证码识别请求出错后的重试次数
    private static final int CAPTCHA_RETRY_TIMES = 3;

    private static String DLLPATH = null;
    private static String LIBPATH = null;
    private static int libIndex = 0;
    private static int CODELENTH = 8;

    static {
        try {
            Class.forName(ZimuService.class.getName());
            String path = CommonUtil.getResourcePath();
            DLLPATH = path + "basic.dll";
            LIBPATH = path + "special.lib";
            libIndex = CodeRec.INSTANCE.LoadLibFromFile(LIBPATH, "123");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CodeRec extends StdCallLibrary {

        CodeRec INSTANCE = (CodeRec) Native.loadLibrary(DLLPATH, CodeRec.class);

        int LoadLibFromFile(String path, String pwd);

        boolean GetCodeFromBuffer(int index, byte[] img, int len, byte[] code);
    }

    /**
     * 将验证码转换为base64字符串
     *
     * @param url 验证码URL
     * @return base64字符串
     * @throws Exception
     */
    public static String captcha2Base64(String url, List<Header> headerList, HttpHost proxy, CookieStore cookieStore)
        throws Exception {
        return new String(Base64Util.encode(captcha2Bytes(url, headerList, proxy, cookieStore)));
    }

    /**
     * 根据url获取验证码图片，并将其转换为字节数组
     *
     * @param url 验证码URL
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] captcha2Bytes(String url, List<Header> headerList, HttpHost proxy, CookieStore cookieStore)
        throws Exception {
        HttpGet httpGet = new HttpGet(url);
        if (null != headerList && headerList.size() > 1) {
            httpGet.setHeaders(headerList.toArray(new Header[0]));
        }
        ResponseHandler<byte[]> responseHandler = HttpClientUtil.getByteArrayResponseHandler();
        return HttpClientUtil.execute(httpGet, 3000, 3000, proxy, cookieStore, responseHandler);
    }

    /**
     * 调用动态库进行验证码解析
     *
     * @param img
     * @return
     */
    private static String sundayDecode(byte[] img) {
        byte[] code = new byte[CODELENTH];
        String rtnCode = null;
        boolean result = CodeRec.INSTANCE.GetCodeFromBuffer(libIndex, img, img.length, code);
        if (result) {
            try {
                rtnCode = new String(code, "GBK");
            } catch (UnsupportedEncodingException e) {
                logger.error("sunday Decode error", e);
                return null;
            }
            rtnCode = rtnCode.trim();
            return rtnCode.trim();
        }
        return null;
    }

    public static String getCaptchaText(String url, List<Header> headerList, HttpHost proxy, CookieStore cookieStore) {
        String result = "";
        try {
            String solution = "self";
            byte[] img = captcha2Bytes(url, headerList, proxy, cookieStore);
            if (img == null || img.length == 0) {
                return null;
            }

            if ("basic".equals(solution)) {
                result = sundayDecode(img);
            } else if ("special".equals(solution)) {
                // 将b作为输入流；
                ByteArrayInputStream in = new ByteArrayInputStream(img);
                //// 将in作为输入流，读取图片存入image中，而这里in可以为ByteArrayInputStream();
                BufferedImage image = ImageIO.read(in);
                Integer number = calculate(image);
                if (null != number) {
                    result = String.valueOf(number);
                }
            }
        } catch (Exception e) {
            logger.error("getCaptchaText error", e);
        }
        return result;
    }

    /**
     * 仅针对运算类验证码破解
     *
     * @return
     */
    public static Integer calculate(BufferedImage bufferedImage) {
        try {
            bufferedImage = ImageUtil.binary(ImageUtil.setBoder(bufferedImage), 200);

            // 分割验证码，以中间空白为分割
            List<BufferedImage> bufferedImages = ImageUtil.verticalCutting(bufferedImage, 4);
            // 匹配分割的字符串，解决不了黏连问题
            String validateCode = ImageUtil.matchnew(bufferedImages, ZimuService.ZIMU_PATH);
            if (!ImageUtil.decide(ImageUtil.correct(validateCode))) {
                // 因为黏连以分析图片字典的形式做解析
                validateCode = Recognizer.singleton.recognize(bufferedImage, ZimuService.ZIMU_PATH,
                    WanderConfig.getDefault());
            }

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            engine.put("x", 10);
            engine.put("y", 10);

            if (StringUtils.isEmpty(validateCode)) {
                return null;
            }

            return Double.valueOf(
                engine.eval(ImageUtil.correct(validateCode).replaceAll("÷", "/").replaceAll("×", "*")).toString())
                .intValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 获取验证码，添加代理，添加统计数据，添加重试
     *
     * @param captchaUrl
     * @param proxy
     * @param cookieStore
     * @return
     */
    public static String getCaptcha(String captchaUrl, List<Header> headerList, HttpHost proxy,
                                    BasicCookieStore cookieStore) {
        // 验证码识别
        int retry = 0;
        String captcha = "";
        while (true) {
            try {
                captcha = CaptchaUtil.getCaptchaText(captchaUrl, headerList, proxy, cookieStore);
                if (StringUtils.isNotEmpty(captcha) && !"null".equals(captcha)) {
                    break;
                }
            } catch (Exception e) {
                logger.error("Decode Captcha Error", e);
            } finally {
                if (retry++ > CAPTCHA_RETRY_TIMES) {
                    break;
                }
            }
        }
        // 验证码识别失败
        if (StringUtils.isEmpty(captcha)) {
            return null;
        }
        return captcha;
    }
}

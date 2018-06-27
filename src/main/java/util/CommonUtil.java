package util;

import java.net.URL;

public class CommonUtil {

    public static String getResourcePath() {
        final URL resource = CommonUtil.class.getClassLoader().getResource("basic.dll");
        final String path = resource.getPath();
        return path.substring(1, path.lastIndexOf("/") + 1);
    }
}

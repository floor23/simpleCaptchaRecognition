package ocr;

import util.CommonUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字母词典缓存，根据路径获取缓存
 *
 */
public class ZimuService {
	private static ConcurrentHashMap<String, Map<BufferedImage, String>> cache = new ConcurrentHashMap<String, Map<BufferedImage, String>>();
	public static final String ZIMU_PATH = CommonUtil.getResourcePath() + "calculate/";

	static {
		init();
	}

	public static Map<BufferedImage, String> getZimo(String zimoPath) {
		return cache.get(zimoPath);
	}

	public static void init() {
		try {
			Map<BufferedImage, String> zimu = loadDirLayerZhimu(ZIMU_PATH);
			cache.putIfAbsent(ZIMU_PATH, zimu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<BufferedImage, String> loadDirLayerZhimu(String dirPath) throws IOException {
		Map<BufferedImage, String> zhimou = new HashMap<BufferedImage, String>();
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for (File file : files) {
			try {
				if (file.isDirectory()) {
					Map<BufferedImage, String> fzhimou = loadDirLayerZhimu(file.getPath());
					if (fzhimou != null && fzhimou.size() > 0) {
						zhimou.putAll(fzhimou);
					}
				} else {
					BufferedImage img = ImageIO.read(file);
					if (img != null) {
						String filename = file.getName();
						// int index = filename.indexOf(".");
						String zhimu = filename.substring(0, 1);
						zhimou.put(ImageUtil.binarizing(img, 250), zhimu);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return zhimou;
	}

	public static String match(List<BufferedImage> imges, String zhimoupath) throws IOException {

		Map<BufferedImage, String> zhimou = loadDirLayerZhimu(zhimoupath);

		Set<BufferedImage> zhimuSet = zhimou.keySet();

		String result = "";

		for (BufferedImage sourceImage : imges) {
			long maxCount = 0;
			BufferedImage maxSamePixelImage = null;

			for (BufferedImage targetImage : zhimuSet) {
				int[] sourceImageArray = ImageUtil.toIntArray(sourceImage);
				int[] targetImageArray = ImageUtil.toIntArray(targetImage);

				long count = ImageUtil.calSamePixelNum(sourceImageArray, targetImageArray);
				if (count > maxCount) {
					maxCount = count;
					maxSamePixelImage = targetImage;
				}

			}
			result = result + zhimou.get(maxSamePixelImage);

		}

		return result;
	}

}

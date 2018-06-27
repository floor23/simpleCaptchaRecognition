package ocr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片字典工厂类
 *
 */
public class FontModelService {
	private static ConcurrentHashMap<String, List<FontModel>> cache = new ConcurrentHashMap<String, List<FontModel>>();

	/**
	 * 获取路径内的图片字典词典模型
	 * 
	 * @param path
	 *            图片字典存放路径
	 * @param tailorModel
	 *            是否去掉指定颜色的无用方框区域（指定非黑色区域）
	 * @return
	 */
	public static List<FontModel> getFontModelList(String path, boolean tailorModel) {
		List<FontModel> fontModelList = cache.get(path);
		if (fontModelList != null) {
			return fontModelList;
		}

		final File file = new File(path);
		try {
			fontModelList = new ArrayList<FontModel>();
			File[] fontFiles = file.listFiles();
			for (File fontFile : fontFiles) {
				BufferedImage bufferedImage = ImageUtil.read(fontFile);
				bufferedImage = ImageUtil.binary(bufferedImage, 400);
				if (tailorModel)
					bufferedImage = ImageUtil.getValidImage(bufferedImage, Color.black);
				String name = fontFile.getName();
				int[][] lattice = ImageUtil.getLattice(bufferedImage);
				String value = name.substring(0, 1);
				FontModel fontModel = new FontModel(name, lattice, value);
				fontModelList.add(fontModel);
			}
			cache.putIfAbsent(path, fontModelList);
			return fontModelList;
		} catch (Exception e) {

		}
		return null;
	}

}

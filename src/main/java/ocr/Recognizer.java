package ocr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * 验证码识别
 *
 */
public class Recognizer {
	public static final Recognizer singleton = new Recognizer();

	/**
	 * 验证码图片识别
	 * 
	 * @param bufferedImage
	 * @param modelFilePath
	 * @return
	 */
	public String recognize(BufferedImage bufferedImage, String modelFolderPath) {
		return recognize(bufferedImage, modelFolderPath, WanderConfig.getDefault());
	}

	/**
	 * 验证码图片识别
	 * 
	 * @param bufferedImage
	 * @param modelFolderPath
	 * @param saltSizeThreshold
	 *            椒盐点像素数量阈值
	 * @return
	 */
	public String recognize(BufferedImage bufferedImage, String modelFolderPath, WanderConfig config) {
		try {
			List<FontModel> fontModelList = FontModelService.getFontModelList(modelFolderPath, config.isTailorModel());
			StringBuilder sb = new StringBuilder();
			int startX = 0;
			while (true) {
				startX = getStartX(bufferedImage, startX, config.getSaltSizeThreshold());
				if (startX < 0)
					break;
				FontModel currentModel = null;
				double maxRate = 0;
				int perfectX = 0, perfectY = 0;
				for (FontModel fontModel : fontModelList) {
					int startY = getStartY(bufferedImage, fontModel, startX, config.getyRevise());
					if (startY < 0)
						break;
					Out<Integer> currentX = new Out<Integer>(startX);
					Out<Integer> currentY = new Out<Integer>(startY);
					double rate = compare(bufferedImage, fontModel, config, currentX, currentY);
					if (rate > maxRate) {
						maxRate = rate;
						currentModel = fontModel;
						perfectX = currentX.get();
						perfectY = currentY.get();
					}

				}
				if (currentModel == null)
					break;
				bufferedImage = removeChar(bufferedImage, currentModel, perfectX, perfectY);
				if (maxRate > 0.6)
					sb.append(currentModel.getValue());
				else
					sb.append(FontModel.getDefvalue());
				startX = perfectX + currentModel.getWidth();
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private int getStartX(BufferedImage bufferedImage, int startX, int saltSizeThreshold) {
		// 噪点集合
		Set<Point> saltSet = new HashSet<Point>();
		for (int x = startX; x < bufferedImage.getWidth(); x++) {
			for (int y = 0; y < bufferedImage.getHeight(); y++) {
				if (bufferedImage.getRGB(x, y) == Color.black.getRGB() && !saltSet.contains(new Point(x, y))) {
					Set<Point> set = getBlock(bufferedImage, new Point(x, y), saltSizeThreshold);
					if (set.size() < saltSizeThreshold) {
						saltSet.addAll(set);
						continue;
					}
					return x;
				}
			}
		}
		return -1;
	}

	/**
	 * 获取连通面积,若大于等于threshold则返回
	 * 
	 * @param bufferedImage
	 * @param point
	 * @param threshold
	 *            像素点数阈值
	 * @return
	 */
	private static Set<Point> getBlock(BufferedImage bufferedImage, Point point, int threshold) {
		Set<Point> set = new HashSet<Point>();
		Stack<Point> stack = new Stack<Point>();
		stack.push(point);
		while (!stack.empty()) {
			Point p = stack.pop();
			set.add(p);
			if (threshold > 0 && set.size() >= threshold)
				break;
			Point poi;
			// 左上
			poi = new Point(p.x - 1, p.y - 1);
			if (!set.contains(poi) && poi.x > 0 && poi.y > 0
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 上
			poi = new Point(p.x, p.y - 1);
			if (!set.contains(poi) && poi.y > 0 && bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 右上
			poi = new Point(p.x + 1, p.y - 1);
			if (!set.contains(poi) && poi.x < bufferedImage.getWidth() && poi.y > 0
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 左
			poi = new Point(p.x - 1, p.y);
			if (!set.contains(poi) && poi.x > 0 && bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 右
			poi = new Point(p.x + 1, p.y);
			if (!set.contains(poi) && poi.x < bufferedImage.getWidth()
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 左下
			poi = new Point(p.x - 1, p.y + 1);
			if (!set.contains(poi) && poi.x > 0 && poi.y < bufferedImage.getHeight()
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 下
			poi = new Point(p.x, p.y + 1);
			if (!set.contains(poi) && poi.y < bufferedImage.getHeight()
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
			// 右下
			poi = new Point(p.x + 1, p.y + 1);
			if (!set.contains(poi) && poi.x < bufferedImage.getWidth() && poi.y < bufferedImage.getHeight()
					&& bufferedImage.getRGB(poi.x, poi.y) == Color.black.getRGB())
				stack.push(poi);
		}
		return set;
	}

	private int getStartY(BufferedImage bufferedImage, FontModel fontModel, int startX, YRevise yRevise) {
		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = startX; x < bufferedImage.getWidth() && x < (startX + fontModel.getWidth()); x++) {
				if (bufferedImage.getRGB(x, y) == Color.black.getRGB()) {
					boolean boolContinue = true;
					if (x + 1 < bufferedImage.getWidth() && bufferedImage.getRGB(x + 1, y) == Color.black.getRGB())
						boolContinue = false;
					if (x + 1 < bufferedImage.getWidth() && y + 1 < bufferedImage.getHeight()
							&& bufferedImage.getRGB(x + 1, y + 1) == Color.black.getRGB())
						boolContinue = false;
					if (y + 1 < bufferedImage.getHeight() && bufferedImage.getRGB(x, y + 1) == Color.black.getRGB())
						boolContinue = false;
					if (x - 1 >= 0 && y + 1 < bufferedImage.getHeight()
							&& bufferedImage.getRGB(x - 1, y + 1) == Color.black.getRGB())
						boolContinue = false;
					if (boolContinue)
						continue;
					return yRevise.revise(y);
				}
			}
		}
		return -1;
	}

	private double compare(BufferedImage bufferedImage, FontModel fontModel, WanderConfig config, Out<Integer> startX,
			Out<Integer> startY) {
		int[] xDrift = config.getxDrift();
		int[] yDrift = config.getyDrift();
		int currentX = 0, currentY = 0, perfectX = 0, perfectY = 0;
		double maxRate = 0;
		for (int x : xDrift) {
			currentX = startX.get() + x;
			if (currentX >= 0 && currentX < bufferedImage.getWidth()) {
				for (int y : yDrift) {
					currentY = startY.get() + y;
					if (currentY >= 0 && currentY < bufferedImage.getHeight()) {
						double rate = compare(bufferedImage, fontModel, currentX, currentY);
						if (rate > maxRate) {
							maxRate = rate;
							perfectX = currentX;
							perfectY = currentY;
						}
					}
				}
			}

		}
		startX.set(perfectX);
		startY.set(perfectY);
		return maxRate;
	}

	private double compare(BufferedImage bufferedImage, FontModel fontModel, int startX, int startY) {
		int total = 0;
		int hit = 0;
		for (int x = 0; (x < fontModel.getWidth() && (startX + x) < bufferedImage.getWidth()); x++) {
			for (int y = 0; y < fontModel.getHeight() && (startY + y) < bufferedImage.getHeight(); y++) {
				total++;
				Color color = fontModel.getLattice()[y][x] == 1 ? Color.black : Color.white;
				if (bufferedImage.getRGB(startX + x, startY + y) == color.getRGB())
					hit++;
			}
		}
		if (total == 0)
			return 0;
		return hit * 1.0 / total;
	}

	private BufferedImage removeChar(BufferedImage bufferedImage, FontModel fontModel, int startX, int startY) {
		for (int x = 0; x < fontModel.getWidth() && (x + startX) < bufferedImage.getWidth(); x++) {
			for (int y = 0; y < fontModel.getHeight() && (y + startY) < bufferedImage.getHeight(); y++) {
				if (fontModel.getLattice()[y][x] == 1
						&& bufferedImage.getRGB(startX + x, startY + y) == Color.black.getRGB()) {
					bufferedImage.setRGB(startX + x, startY + y, Color.white.getRGB());
				}
			}
		}
		return bufferedImage;
	}
}

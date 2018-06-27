package ocr;

import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ImageUtil {

	public static BufferedImage gray(BufferedImage bufferedImage) {
		for (int x = 0; x < bufferedImage.getWidth(); x++) {
			for (int y = 0; y < bufferedImage.getHeight(); y++) {
				int rgb = bufferedImage.getRGB(x, y);
				int a = rgb & 0xff000000;
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				r = (r + 255) / 2;
				g = (g + 255) / 2;
				b = (b + 255) / 2;
				int grayRgb = a | (r << 16) | (g << 8) | b;
				bufferedImage.setRGB(x, y, grayRgb);
			}
		}
		return bufferedImage;
	}

	public static BufferedImage binary(BufferedImage bufferedImage, int threshold) {
		for (int x = 0; x < bufferedImage.getWidth(); ++x) {
			for (int y = 0; y < bufferedImage.getHeight(); ++y) {
				int rgb = bufferedImage.getRGB(x, y);
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = rgb & 0xff;
				if ((r + g + b) <= threshold) {
					bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					bufferedImage.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return bufferedImage;
	}

	/**
	 * 去掉指定颜色的无用方框区域
	 * 
	 * @param bufferedImage
	 * @param color
	 *            指定边框区域的颜色
	 * @return
	 */
	public static BufferedImage getValidImage(BufferedImage bufferedImage, Color color) {
		int minX = bufferedImage.getWidth();
		int minY = bufferedImage.getHeight();
		int maxX = 0;
		int maxY = 0;
		for (int x = 0; x < bufferedImage.getWidth(); ++x) {
			for (int y = 0; y < bufferedImage.getHeight(); ++y) {
				if (bufferedImage.getRGB(x, y) == color.getRGB()) {
					if (minX > x)
						minX = x;
					if (minY > y)
						minY = y;
					if (maxX < x)
						maxX = x;
					if (maxY < y)
						maxY = y;
				}
			}
		}
		return bufferedImage.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}

	/**
	 * 去除图片的白色无用方框区域
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static BufferedImage getValidImage(BufferedImage bufferedImage) {
		int minX = bufferedImage.getWidth();
		int minY = bufferedImage.getHeight();
		int maxX = 0;
		int maxY = 0;
		for (int x = 0; x < bufferedImage.getWidth(); ++x) {
			for (int y = 0; y < bufferedImage.getHeight(); ++y) {
				if (bufferedImage.getRGB(x, y) != Color.WHITE.getRGB()) {
					if (minX > x)
						minX = x;
					if (minY > y)
						minY = y;
					if (maxX < x)
						maxX = x;
					if (maxY < y)
						maxY = y;
				}
			}
		}
		return bufferedImage.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}

	/**
	 * 获取图片点阵<br/>
	 * 只兼容黑白，把非黑点全部设置为了0，黑点为1；
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static int[][] getLattice(BufferedImage bufferedImage) {
		int[][] lattice = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];
		for (int x = 0; x < bufferedImage.getWidth(); ++x) {
			for (int y = 0; y < bufferedImage.getHeight(); ++y) {
				if (bufferedImage.getRGB(x, y) == Color.black.getRGB()) {
					lattice[y][x] = 1;
				} else {
					lattice[y][x] = 0;
				}
			}
		}
		return lattice;
	}

	public static boolean compareLattice(int[][] l1, int[][] l2) {
		if (l1.length != l2.length)
			return false;
		if (l1[0].length != l2[0].length)
			return false;
		int width = l1[0].length;
		int height = l1.length;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (l1[y][x] != l2[y][x])
					return false;
			}
		}
		return true;
	}

	public static void save(BufferedImage bufferedImage, String filePath) throws Exception {
		File file = new File(filePath);
		FileUtil.checkParentFile(file);
		ImageIO.write(bufferedImage, "JPEG", file);
	}

	public static void checkParentFile(File file) throws Exception {
		File parent = file.getParentFile();
		if (parent != null && parent.exists() == false) {
			if (parent.mkdirs() == false) {
				throw new IOException("File '" + file + "' could not be created");
			}
		}
	}

	public static BufferedImage read(File file) throws Exception {
		return ImageIO.read(file);
	}

	public static BufferedImage read(String filePath) throws Exception {
		return read(new File(filePath));
	}

	public static BufferedImage generateImage(String str, int width, int height, Font font) {
		BufferedImage buffImage = null;
		Graphics2D graphics = null;
		try {
			buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			graphics = buffImage.createGraphics();
			// graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			graphics.setColor(Color.black);
			graphics.setFont(font);
			int xPoint = 0;
			graphics.drawString(str, xPoint, height - 4);
			return buffImage;
		} catch (Exception e) {
			return null;
		} finally {
			graphics.dispose();
		}
	}

	/**
	 * 清除边框
	 * 
	 * @return
	 */
	public static BufferedImage setBoder(BufferedImage bufferedImage) {
		return setBoder(bufferedImage, 1, 1, 1, 1);
	}

	/**
	 * 0 不清除边框, 1 清除上 0001, 2清除 下 0010, 4清除左 0100, 8 清除右 1000
	 * 
	 * @return
	 */
	public static BufferedImage setBoder(BufferedImage bufferedImage, int top, int bottom, int left, int right) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		for (int x = 0; x < width; ++x) {
			if (top == 1) {
				bufferedImage.setRGB(x, 0, Color.WHITE.getRGB());
			}

			if (bottom == 1) {
				bufferedImage.setRGB(x, height - 1, Color.WHITE.getRGB());
			}
		}
		for (int y = 0; y < height; ++y) {
			if (left == 1) {
				bufferedImage.setRGB(0, y, Color.WHITE.getRGB());
			}
			if (right == 1) {
				bufferedImage.setRGB(width - 1, y, Color.WHITE.getRGB());
			}
		}
		return bufferedImage;
	}

	/**
	 * 垂直切割
	 * 
	 * @param bufferdImage
	 * @param startx
	 * @return
	 */
	public static List<BufferedImage> verticalCutting(BufferedImage bufferdImage, int numOfChar) {
		List<BufferedImage> subImage = new ArrayList<BufferedImage>();
		int width = bufferdImage.getWidth();
		int height = bufferdImage.getHeight();
		int startx = 0;
		for (int i = 0; i < numOfChar; i++) {
			int x1 = -1, x2 = 0;

			for (int x = startx; x < width; x++) {
				boolean hasBlack = false;
				for (int y = 0; y < height; y++) {
					if (bufferdImage.getRGB(x, y) == Color.BLACK.getRGB()) {
						hasBlack = true;
						if (x1 == -1) {
							x1 = x;
						}
					}
				}
				if (!hasBlack && x1 != -1) {
					x2 = x - 1;
					startx = x;
					break;
				}
			}
			if (x1 >= 0) {
				subImage.add(bufferdImage.getSubimage(x1, 0, x2 - x1 + 1, height));
			}

		}
		return subImage;

	}

	public static String matchnew(List<BufferedImage> imges, String zimopath) throws IOException {
		Map<BufferedImage, String> zhimou = ZimuService.getZimo(zimopath);

		Set<BufferedImage> zhimuSet = zhimou.keySet();
		String result = "";

		for (BufferedImage sourceImage : imges) {
			long maxCount = 0;
			BufferedImage maxSamePixelImage = null;

			for (BufferedImage targetImage : zhimuSet) {
				int[] sourceImageArray = toIntArray(sourceImage);
				int[] targetImageArray = toIntArray(targetImage);

				long count = calSamePixelNum(sourceImageArray, targetImageArray);
				if (count > maxCount) {
					maxCount = count;
					maxSamePixelImage = targetImage;
				}
			}
			result = result + zhimou.get(maxSamePixelImage);
		}

		return result;
	}

	public static int[] toIntArray(BufferedImage bufferdImage) {
		int width = bufferdImage.getWidth();
		int height = bufferdImage.getHeight();
		int[] arr = new int[width * height];
		int i = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int[] rgb1 = getStandardRGB(bufferdImage.getRGB(x, y));
				arr[i] = rgb1[0] + rgb1[1] + rgb1[2];
				i++;
			}
		}
		return arr;
	}

	/**
	 * 图片二值化 图像的二值化，就是将图像上的像素点的灰度值设置为0或255，也就是将整个图像呈现出明显的只有黑和白的视觉效果。
	 * 
	 * @param bufferedImage
	 * @param grayThreshold
	 *            灰度阀值 小于等于此灰度设置为黑色，大于设置为白色
	 * @return
	 */
	public static BufferedImage binarizing(BufferedImage bufferedImage, int grayThreshold) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int pixel = bufferedImage.getRGB(x, y);
				int r = (pixel & 0xff0000) >> 16;
				int g = (pixel & 0xff00) >> 8;
				int b = (pixel & 0xff);
				if ((r + g + b) <= grayThreshold) {
					bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					bufferedImage.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return bufferedImage;
	}

	public static long calSamePixelNum(int[] source, int[] target) {
		int length = source.length <= target.length ? source.length : target.length;
		long samePixelNum = 0;
		for (int i = 0; i < length; i++) {
			int[] rgb1 = getStandardRGB(source[i]);
			int[] rgb2 = getStandardRGB(target[i]);
			if ((rgb1[0] + rgb1[1] + rgb1[2]) == (rgb2[0] + rgb2[1] + rgb2[2])) {
				samePixelNum++;
			}
		}

		return samePixelNum;
	}

	public static int[] getStandardRGB(int pixel) {
		int[] rgb = new int[3];
		rgb[0] = (pixel & 0xff0000) >> 16;
		rgb[1] = (pixel & 0xff00) >> 8;
		rgb[2] = (pixel & 0xff);

		return rgb;
	}

	public static int getStandardRGBNum(int pixel) {
		int[] rgb = getStandardRGB(pixel);
		return rgb[0] + rgb[1] + rgb[2];
	}

	public static String correct(String value) {
		while (StringUtils.isNotEmpty(value) && value.length() > 0) {
			if (value.endsWith("+") || value.endsWith("-") || value.endsWith("÷") || value.endsWith("×")
					|| value.endsWith("=")) {
				value = value.substring(0, value.length() - 1);
			} else {
				break;
			}
		}
		return value;
	}

	public static boolean decide(String value) {
		if (value.length() >= 3) {
			if (value.replaceAll("(?:\\+|-|÷|x)", "").length() >= 2) {
				return true;
			}
		}
		return false;
	}

}

package ocr;

/**
 * 验证码识别参数配置
 *
 */
public class WanderConfig {
	/** 椒盐点像素数量阈值(小于这个阈值的色块被认为是干扰线) */
	private int saltSizeThreshold;

	/** ocr比较的y轴修正(适用于存在干扰严重切字符高度固定的图像) */
	private YRevise yRevise;

	/** ocr比较时,x轴漂移范围 */
	private int[] xDrift;

	/** ocr比较时,y轴漂移范围 */
	private int[] yDrift;

	/** 是否去掉指定颜色的无用方框区域（指定非黑色区域） */
	private boolean tailorModel;

	public WanderConfig(int saltSizeThreshold, YRevise yRevise, int[] xDrift, int[] yDrift, boolean tailorModel) {
		super();
		this.saltSizeThreshold = saltSizeThreshold;
		this.yRevise = yRevise;
		this.xDrift = xDrift;
		this.yDrift = yDrift;
		this.tailorModel = tailorModel;
	}

	public int getSaltSizeThreshold() {
		return saltSizeThreshold;
	}

	public void setSaltSizeThreshold(int saltSizeThreshold) {
		this.saltSizeThreshold = saltSizeThreshold;
	}

	public YRevise getyRevise() {
		return yRevise;
	}

	public void setyRevise(YRevise yRevise) {
		this.yRevise = yRevise;
	}

	public int[] getxDrift() {
		return xDrift;
	}

	public void setxDrift(int[] xDrift) {
		this.xDrift = xDrift;
	}

	public int[] getyDrift() {
		return yDrift;
	}

	public void setyDrift(int[] yDrift) {
		this.yDrift = yDrift;
	}

	public boolean isTailorModel() {
		return tailorModel;
	}

	public void setTailorModel(boolean tailorModel) {
		this.tailorModel = tailorModel;
	}

	public static WanderConfig getDefault() {
		int[] drift = { -1, 0, 1 };
		return new WanderConfig(10, YRevise.getDefault(), drift, drift, true);
	}
}

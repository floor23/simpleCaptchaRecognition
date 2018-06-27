package ocr;

/**
 * 适用于存在干扰严重切字符高度固定的图像 对y轴起始对比位置因干扰线导致的不准确进行修正
 * 
 * @author nicolas
 * 
 */
public class YRevise {

	/** y轴起始位置阈值 */
	private int yThreshold;

	/** 当y轴起始位置小于yThreshold,则将y轴起始位置设置为yyBasic */
	private int yBasic;

	public YRevise(int yThreshold, int yBasic) {
		super();
		this.yThreshold = yThreshold;
		this.yBasic = yBasic;
	}

	public int getyThreshold() {
		return yThreshold;
	}

	public void setyThreshold(int yThreshold) {
		this.yThreshold = yThreshold;
	}

	public int getyBasic() {
		return yBasic;
	}

	public void setyBasic(int yBasic) {
		this.yBasic = yBasic;
	}

	public int revise(int y) {
		if (y < yThreshold)
			return yBasic;
		return y;
	}

	public static YRevise getDefault() {
		return new YRevise(0, 0);
	}
}

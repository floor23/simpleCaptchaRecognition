package ocr;

/**
 * 图片字典模型
 *
 */
public class FontModel {
	private final static String defValue = "*";

	/** 文件名称 */
	private String name;

	/** 图片点阵 */
	private int[][] lattice;

	/** 字母词典代表的数值 */
	private String value;

	public FontModel(String name, int[][] lattice, String value) {
		super();
		this.name = name;
		this.lattice = lattice;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int[][] getLattice() {
		return lattice;
	}

	public void setLattice(int[][] lattice) {
		this.lattice = lattice;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static String getDefvalue() {
		return defValue;
	}

	public int getWidth() {
		return lattice[0].length;
	}

	public int getHeight() {
		return lattice.length;
	}

	public void print() {
		for (int[] is : lattice) {
			for (int i : is) {
				System.out.print(i);
			}
			System.out.println();
		}
	}
}

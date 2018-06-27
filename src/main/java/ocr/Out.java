package ocr;


public class Out<T> {
	
	public Out(T t) {
		this.t = t;
	}

	private T t;

	public T get() {
		return t;
	}

	public void set(T t) {
		this.t = t;
	}
}

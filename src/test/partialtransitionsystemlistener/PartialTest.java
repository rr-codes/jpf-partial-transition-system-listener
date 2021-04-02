package partialtransitionsystemlistener;

import java.util.Random;

public class PartialTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Random r = new Random();

		if (r.nextBoolean()) {
			if (r.nextBoolean()) {
				String s = "oggy";
			} else {
				String s = "wazz";
			}
		} else {
			if (r.nextBoolean()) {
				String s = "kunn";
			} else {
				String s = "boggy";
			}
		}
	}
}

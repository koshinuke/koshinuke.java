package org.koshinuke.jersey;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class ContentFilterTest {

	@Test
	public void testRegex() throws Exception {
		String staticPattern = "[sS][tT][aA][tT][iI][cC]";
		String uriSegment = "([\\w\\-\\.!~\\*'\\(\\):@&=\\+\\$,]|%[0-9a-zA-Z]{2})+";
		Pattern p = Pattern.compile("/(" + staticPattern + "|" + uriSegment
				+ "/" + uriSegment + "\\.[gG][iI][tT])(/.*)?");
		System.out.println(p.pattern());
		matchTrue(p, "/static/images/hoge.gif");
		matchTrue(p, "/sTatIc/css/fuga.css");

		matchTrue(p, "/aa/piro.js.git");
		matchTrue(p, "/aa/piro.js.Git/hogehoge%22");
		matchTrue(p, "/aa/piro..js.giT/hogehoge%22?aa=bb&cc=dd");
		matchTrue(p, "/aa%20/piro.git");

		matchFalse(p, "/330%21/css/fuga.css");
		matchFalse(p, "/aa%2/js/piro.js");
	}

	static void matchTrue(Pattern p, String value) {
		assertTrue(p.matcher(value).matches());
	}

	static void matchFalse(Pattern p, String value) {
		assertFalse(p.matcher(value).matches());
	}
}

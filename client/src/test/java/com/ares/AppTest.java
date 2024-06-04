package com.ares;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testLeaderBoard(){

    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        String str = "123321";
        assert isPalindrome(str);
        String str1 = "12321";
        assert isPalindrome(str1);
        String str2 = "@12321";
        assert isPalindrome(str2);
        String str3 = "12@321";
        assert isPalindrome(str3);
        String str4 = "1a@321";
        assert isPalindrome(str4)== false;
        String str5 = "1321";
        assert isPalindrome(str5)== false;
    }

    public boolean isPalindrome(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        int leftIndex = 0;
        int rightIndex = str.length() - 1;

        char letfLowerChar;
        char rightLowerChar;
        while (leftIndex <= rightIndex) {
            if (!Character.isLetter(str.charAt(leftIndex)) && !Character.isDigit(str.charAt(leftIndex))) {
                leftIndex++;
                continue;
            }
            if (!Character.isLetter(str.charAt(rightIndex)) && !Character.isDigit(str.charAt(rightIndex))) {
                rightIndex--;
                continue;
            }

            letfLowerChar = Character.toLowerCase(str.charAt(leftIndex));
            rightLowerChar = Character.toLowerCase(str.charAt(rightIndex));
            if (letfLowerChar != rightLowerChar) {
                return false;
            }
            leftIndex++;
            rightIndex--;
        }
        return true;
    }
}

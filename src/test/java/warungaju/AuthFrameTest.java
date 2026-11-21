package warungaju;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuthFrameTest {
    @Test
    void passwordStrengthMatchesTheRegistrationRequirements() {
        assertEquals(0, AuthFrame.passwordCriteriaScore(new char[0]));
        assertEquals(1, AuthFrame.passwordCriteriaScore("abc".toCharArray()));
        assertEquals(2, AuthFrame.passwordCriteriaScore("abc123".toCharArray()));
        assertEquals(4, AuthFrame.passwordCriteriaScore("Abcdefg1".toCharArray()));
        assertEquals(5, AuthFrame.passwordCriteriaScore("Abcdefg1!".toCharArray()));
        assertEquals("Sangat lemah", AuthFrame.passwordStrengthLevel(1));
        assertEquals("Lemah", AuthFrame.passwordStrengthLevel(2));
        assertEquals("Kuat", AuthFrame.passwordStrengthLevel(4));
        assertEquals("Sangat kuat", AuthFrame.passwordStrengthLevel(5));
    }
}

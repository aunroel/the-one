package sh;

import java.util.Random;

public class RandomStringGenerator {

    private final int SHORTEST_MESSAGE_LENGTH = 7;
    private final int LONGEST_MESSAGE_LENGTH = 70;

    public RandomStringGenerator() {}

    public String randomBoundedStringGenerator() {
        int leftLimit = 32; // space character
        int rightLimit = 126; // '~' character
        Random random = new Random();
        int targetStringLength = random.ints(SHORTEST_MESSAGE_LENGTH, (LONGEST_MESSAGE_LENGTH + 1)).limit(1).findFirst().getAsInt();

        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}

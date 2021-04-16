package uk.gov.hmcts.reform.cpo.befta;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

public class CaseIdGenerator {

    public static void main(String[] args) {
        new CaseIdGenerator().generateValidCaseReference();
    }

    public String generateValidCaseReference(){
        String timestamp = getCurrentTime();
        String randomFour = genRandomFourNumbers();
        int checkedDigit = getCheckDigit(timestamp,randomFour);
        String UID = timestamp + randomFour + checkedDigit;
        System.out.println(UID);
        return UID;
    }

    public String generateInvalidCaseReference(){
        return RandomStringUtils.randomNumeric(16);
    }

    /**
     * This is the timestamp to 10th of the second representing first 10 digits of the UID for a case
     * @return
     */
    private String getCurrentTime(){
        long date = new Date().getTime();
        return String.valueOf(date).substring(0,11);
    }

    private String genRandomFourNumbers(){
        return RandomStringUtils.randomNumeric(4);
    }

    private int getCheckDigit(String timestamp, String randomFour){
        String numberString = timestamp + randomFour;
        return checkSum(numberString, true);
    }

    private int checkSum(String numberString, boolean noCheckDigit) {
        int sum = 0, checkDigit = 0;

        if (!noCheckDigit)
            numberString = numberString.substring(0, numberString.length() - 1);

        boolean isDouble = true;
        for (int i = numberString.length() - 1; i >= 0; i--) {
            int k = Integer.parseInt(String.valueOf(numberString.charAt(i)));
            sum += sumToSingleDigit((k * (isDouble ? 2 : 1)));
            isDouble = !isDouble;
        }

        if ((sum % 10) > 0)
            checkDigit = (10 - (sum % 10));

        return checkDigit;
    }

    private int sumToSingleDigit(int k) {
        if (k < 10)
            return k;
        return sumToSingleDigit(k / 10) + (k % 10);
    }
}

package com.pp.model;

import java.util.List;

import lombok.Data;

@Data
public class BeaconToken {
    private String token;
    private String validFrom;
    private String validThrough;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String tokensToString(List<BeaconToken> beaconTokens) {
        StringBuilder stringBuilder = new StringBuilder();
        for (BeaconToken beaconToken : beaconTokens) {
            stringBuilder.append(bytesToHex(beaconToken.token.getBytes()));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}

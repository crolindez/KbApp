package es.carlosrolindez.kbapp;

/**
 * Created by Carlos on 31/07/2017.
 */

final class SecretClass {
    public static long password(String MAC) {

        String[] macAddressParts = MAC.split(":");
        long littleMac = 0;
        int rotation;
        long code = 0;
        long pin;

        for(int i=2; i<6; i++) {
            Long hex = Long.parseLong(macAddressParts[i], 16);
            littleMac *= 256;
            littleMac += hex;
        }

        rotation = Integer.parseInt(macAddressParts[5], 16) & 0x0f;

        for(int i=0; i<4; i++) {
            Long hex =  Long.parseLong(macAddressParts[i], 16);
            code *= 256;
            code += hex;
        }
        code = code >> rotation;
        code &= 0xffff;

        littleMac &= 0xffff;

        pin = littleMac ^ code;
        pin %= 10000;

        return pin;

    }
}

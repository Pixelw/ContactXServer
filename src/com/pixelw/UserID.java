package com.pixelw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserID {

    List<String> idList = new ArrayList<>();


    public static String getRandom(int length) {
        Random random = new Random();
        StringBuilder numbers = new StringBuilder();

        for (int i = 1; i <= length; i++) {
            numbers.append(random.nextInt(10));
        }
        return numbers.toString();
    }


    public String newUserID (int length) {
        String id;
        while (true) {
            id = getRandom(6);
            if (!idList.contains(id)) {
                idList.add(id);
                break;
            }
        }
        return id;
    }
    
}

package ru.alexey_ovcharov.greenguide.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.alexey_ovcharov.greenguide.mobile.entities.Place;
import ru.alexey_ovcharov.greenguide.mobile.entities.PlaceType;

/**
 * Created by Алексей on 23.04.2017.
 */

public class DbHelper {

    private static DbHelper instance = new DbHelper();

    public static DbHelper getInstance() {
        return instance;
    }

    public List<PlaceType> getPlacesTypes() {
        return Arrays.asList(
                new PlaceType(1, "Свалки"),
                new PlaceType(2, "Велопарковки"),
                new PlaceType(3, "Переработка пластика"),
                new PlaceType(4, "Парки")
        );
    }

    public List<Place> getPlacesByType(int placeTypeId) {
        switch (placeTypeId){
            case 1:
                Place p1 = new Place();
                p1.setAddress("Барнаул, ул. Малахова, 55");
                return Arrays.asList(p1);
            case 2:
                Place p2 = new Place();
                p2.setAddress("Барнаул, ул. Взлетная, 12");
                return Arrays.asList(p2);
            case 3:
                Place p3 = new Place();
                p3.setAddress("Барнаул, ул. Сухэ-Батора, 13");
                return Arrays.asList(p3);
            case 4:
                Place p4 = new Place();
                p4.setAddress("Барнаул, пр-т Космонавтор, 1");
                return Arrays.asList(p4);
        }
        return Collections.EMPTY_LIST;
    }

}

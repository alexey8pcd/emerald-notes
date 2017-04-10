package ru.alexey_ovcharov.webserver;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Countries;
import ru.alexey_ovcharov.webserver.ImagesForPlace;
import ru.alexey_ovcharov.webserver.PlaceTypes;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Places.class)
public class Places_ { 

    public static volatile SingularAttribute<Places, Integer> idPlace;
    public static volatile SingularAttribute<Places, String> address;
    public static volatile SingularAttribute<Places, PlaceTypes> idPlaceType;
    public static volatile SingularAttribute<Places, BigDecimal> latitude;
    public static volatile SingularAttribute<Places, String> description;
    public static volatile SingularAttribute<Places, Date> dateCreate;
    public static volatile CollectionAttribute<Places, ImagesForPlace> imagesForPlaceCollection;
    public static volatile SingularAttribute<Places, Countries> idCountry;
    public static volatile SingularAttribute<Places, BigDecimal> longitude;

}
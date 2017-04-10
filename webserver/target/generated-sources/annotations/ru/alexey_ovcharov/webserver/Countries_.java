package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Places;
import ru.alexey_ovcharov.webserver.Things;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Countries.class)
public class Countries_ { 

    public static volatile SingularAttribute<Countries, String> country;
    public static volatile CollectionAttribute<Countries, Places> placesCollection;
    public static volatile SingularAttribute<Countries, Integer> idCountry;
    public static volatile CollectionAttribute<Countries, Things> thingsCollection;

}
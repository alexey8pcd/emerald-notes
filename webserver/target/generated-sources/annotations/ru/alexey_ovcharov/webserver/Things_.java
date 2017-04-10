package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.CategoriesOfThings;
import ru.alexey_ovcharov.webserver.Countries;
import ru.alexey_ovcharov.webserver.ImagesForThing;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Things.class)
public class Things_ { 

    public static volatile SingularAttribute<Things, Integer> idThing;
    public static volatile SingularAttribute<Things, Integer> idDangerForEnvironment;
    public static volatile SingularAttribute<Things, String> name;
    public static volatile SingularAttribute<Things, String> description;
    public static volatile CollectionAttribute<Things, ImagesForThing> imagesForThingCollection;
    public static volatile SingularAttribute<Things, Integer> decompositionTime;
    public static volatile SingularAttribute<Things, Countries> idCountry;
    public static volatile SingularAttribute<Things, CategoriesOfThings> idCategory;

}
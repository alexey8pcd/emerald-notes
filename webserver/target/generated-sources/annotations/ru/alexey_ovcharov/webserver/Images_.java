package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.ImagesForPlace;
import ru.alexey_ovcharov.webserver.ImagesForThing;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Images.class)
public class Images_ { 

    public static volatile SingularAttribute<Images, byte[]> imageData;
    public static volatile CollectionAttribute<Images, ImagesForThing> imagesForThingCollection;
    public static volatile SingularAttribute<Images, Integer> idImage;
    public static volatile CollectionAttribute<Images, ImagesForPlace> imagesForPlaceCollection;

}
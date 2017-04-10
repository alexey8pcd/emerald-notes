package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Things;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(CategoriesOfThings.class)
public class CategoriesOfThings_ { 

    public static volatile SingularAttribute<CategoriesOfThings, String> category;
    public static volatile SingularAttribute<CategoriesOfThings, Integer> idCategory;
    public static volatile CollectionAttribute<CategoriesOfThings, Things> thingsCollection;

}
package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Messages;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Users.class)
public class Users_ { 

    public static volatile SingularAttribute<Users, Integer> idUser;
    public static volatile CollectionAttribute<Users, Messages> messagesCollection1;
    public static volatile SingularAttribute<Users, String> password;
    public static volatile SingularAttribute<Users, String> login;
    public static volatile SingularAttribute<Users, Integer> idCountry;
    public static volatile CollectionAttribute<Users, Messages> messagesCollection;

}
package ru.alexey_ovcharov.webserver;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Users;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Messages.class)
public class Messages_ { 

    public static volatile SingularAttribute<Messages, Integer> idMessage;
    public static volatile SingularAttribute<Messages, String> theme;
    public static volatile SingularAttribute<Messages, Users> idUserTo;
    public static volatile SingularAttribute<Messages, String> message;
    public static volatile SingularAttribute<Messages, Date> dateSend;
    public static volatile SingularAttribute<Messages, Users> idUserFrom;

}
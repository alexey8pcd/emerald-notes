package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.NoteTypes;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(Notes.class)
public class Notes_ { 

    public static volatile SingularAttribute<Notes, String> noteText;
    public static volatile SingularAttribute<Notes, NoteTypes> idNoteType;
    public static volatile SingularAttribute<Notes, Integer> idNote;

}
package ru.alexey_ovcharov.webserver;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.alexey_ovcharov.webserver.Notes;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2017-04-10T22:58:21")
@StaticMetamodel(NoteTypes.class)
public class NoteTypes_ { 

    public static volatile SingularAttribute<NoteTypes, String> noteType;
    public static volatile SingularAttribute<NoteTypes, Integer> idNoteType;
    public static volatile CollectionAttribute<NoteTypes, Notes> notesCollection;

}
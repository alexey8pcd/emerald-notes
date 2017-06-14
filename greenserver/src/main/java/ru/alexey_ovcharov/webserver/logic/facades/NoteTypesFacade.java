package ru.alexey_ovcharov.webserver.logic.facades;

import javax.ejb.Stateless;
import ru.alexey_ovcharov.webserver.persist.NoteTypes;

/**
@author Alexey
*/
@Stateless
public class NoteTypesFacade extends AbstractFacade<NoteTypes> {

    public NoteTypesFacade() {
        super(NoteTypes.class);
    }

}

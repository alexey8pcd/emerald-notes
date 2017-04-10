package ru.alexey_ovcharov.webserver;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
@author Alexey
*/
@Entity
@Table(name = "messages")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Messages.findAll", query = "SELECT m FROM Messages m")
    , @NamedQuery(name = "Messages.findByIdMessage", query = "SELECT m FROM Messages m WHERE m.idMessage = :idMessage")
    , @NamedQuery(name = "Messages.findByTheme", query = "SELECT m FROM Messages m WHERE m.theme = :theme")
    , @NamedQuery(name = "Messages.findByMessage", query = "SELECT m FROM Messages m WHERE m.message = :message")
    , @NamedQuery(name = "Messages.findByDateSend", query = "SELECT m FROM Messages m WHERE m.dateSend = :dateSend")})
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_message")
    private Integer idMessage;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "theme")
    private String theme;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "message")
    private String message;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_send")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSend;
    @JoinColumn(name = "id_user_from", referencedColumnName = "id_user")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Users idUserFrom;
    @JoinColumn(name = "id_user_to", referencedColumnName = "id_user")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Users idUserTo;

    public Messages() {
    }

    public Messages(Integer idMessage) {
        this.idMessage = idMessage;
    }

    public Messages(Integer idMessage, String theme, String message, Date dateSend) {
        this.idMessage = idMessage;
        this.theme = theme;
        this.message = message;
        this.dateSend = dateSend;
    }

    public Integer getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(Integer idMessage) {
        this.idMessage = idMessage;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDateSend() {
        return dateSend;
    }

    public void setDateSend(Date dateSend) {
        this.dateSend = dateSend;
    }

    public Users getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(Users idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public Users getIdUserTo() {
        return idUserTo;
    }

    public void setIdUserTo(Users idUserTo) {
        this.idUserTo = idUserTo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idMessage != null ? idMessage.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Messages)) {
            return false;
        }
        Messages other = (Messages) object;
        if ((this.idMessage == null && other.idMessage != null) || (this.idMessage != null && !this.idMessage.equals(other.idMessage))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ru.alexey_ovcharov.webserver.Messages[ idMessage=" + idMessage + " ]";
    }

}

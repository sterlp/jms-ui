package org.sterl.jmsui.bl.bookmarks.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.sterl.jmsui.bl.connection.model.JmsConnectionBE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @EqualsAndHashCode(of = "id") @Accessors(chain = true)
@ToString(exclude = "connection")
@Entity
@Table(name = "BOOKMARK")
public class BookmarkBE {

    @Id @GeneratedValue
    private Long id;
    private String name;
    private String type;

    @JsonIgnore
    @ManyToOne(cascade = {}, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false, updatable = false, 
                foreignKey = @ForeignKey(name = "FK_BOOKMARK_TO_CONNECTION"))
    private JmsConnectionBE connection;

    public BookmarkBE(String resource, String type, JmsConnectionBE connection) {
        super();
        this.name = resource;
        this.type = type;
        this.connection = connection;
    }
}

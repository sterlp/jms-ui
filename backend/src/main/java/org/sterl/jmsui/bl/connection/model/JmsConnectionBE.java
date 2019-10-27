package org.sterl.jmsui.bl.connection.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.sterl.jmsui.bl.bookmarks.model.BookmarkBE;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Data @Accessors(chain = true)
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "clientName", "type", "version", "timeout"})
@Entity
@Table(name =  "JMS_CONNECTON", indexes = @Index(name = "IDX_JMS_CONNECTION_TYPE", columnList = "type"))
public class JmsConnectionBE {
    @GeneratedValue
    @Id
    private Long id;

    @NotNull
    @Size(min = 1, max = 128)
    private String type;

    @Version
    private Long version;

    @NotNull
    @Size(min = 1, max = 128)
    private String name;
    
    @Column(name = "client_name")
    @Size(min = 3, max = 128)
    private String clientName = "JMS UI";
    
    private Long timeout;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "connection")
    private List<ConfigValueBE> configValues = new ArrayList<>();
    
    @JsonIgnore @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, mappedBy = "connection", orphanRemoval = true)
    private List<BookmarkBE> bookmarks = new ArrayList<>();
    
    public JmsConnectionBE(Long id) {
        super();
        this.id = id;
    }
    
    public JmsConnectionBE(@NotNull String type, @NotNull @Size(min = 1, max = 128) String name) {
        super();
        this.type = type;
        this.name = name;
    }

    public JmsConnectionBE addOrSetConfig(String key, String value) {
        ConfigValueBE configValueBE = configValues.stream()
            .filter(cv -> key.equals(cv.getName()))
            .findFirst().orElseGet(() -> {
                    ConfigValueBE v = new ConfigValueBE(key, value, this);
                    this.configValues.add(v);
                    return v;
                });

        configValueBE.setValue(value);
        return this;
    }

    /**
     * Removes any config values which aren't part of the given set
     */
    public JmsConnectionBE removeOthers(Collection<String> toRemove) {
        if (!configValues.isEmpty()) {
            Iterator<ConfigValueBE> iterator = configValues.iterator();
            while (iterator.hasNext()) {
                ConfigValueBE cv = iterator.next();
                if (!toRemove.contains(cv.getName())) {
                    iterator.remove();
                }
            }
        }
        return this;
    }
}
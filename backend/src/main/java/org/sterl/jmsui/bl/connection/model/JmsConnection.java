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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "clientName", "type", "version"})
@Entity
@Table(name =  "JMS_CONNECTON", indexes = @Index(name = "IDX_JMS_CONNECTION_TYPE", columnList = "type"))
public class JmsConnection {
    @GeneratedValue
    @Id
    private Long id;

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

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfigValue> configValues = new ArrayList<>();

    public JmsConnection addOrSetConfig(String key, String value) {
        ConfigValue configValue = configValues.stream()
            .filter(cv -> key.equals(cv.getName()))
            .findFirst().orElseGet(() -> {
                    ConfigValue v = new ConfigValue(key, value, this);
                    this.configValues.add(v);
                    return v;
                });

        configValue.setValue(value);
        return this;
    }

    /**
     * Removes any config values which aren't part of the given set
     */
    public JmsConnection removeOthers(Collection<String> keySet) {
        if (!configValues.isEmpty()) {
            Iterator<ConfigValue> iterator = configValues.iterator();
            while (iterator.hasNext()) {
                ConfigValue cv = iterator.next();
                if (!keySet.contains(cv.getName())) {
                    iterator.remove();
                }
                keySet.remove(cv.getName());
            }
        }
        return this;
    }
}
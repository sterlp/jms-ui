package org.sterl.jmsui.bl.connection.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "CONFIG_VALUE", 
    uniqueConstraints = @UniqueConstraint(name = "UC_CONFIG_VALUE", columnNames = {"name", "connection_id"})
)
public class ConfigValueBE {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_role_id_generator")
    @Id
    private Long id;
    @Size(max = 128)
    private String name;
    @Size(max = 255)
    private String value;
    
    @JsonIgnore
    @ManyToOne(cascade = {}, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_CONFIG_VALUE_TO_CONNECTION"))
    private JmsConnectionBE connection;

    public ConfigValueBE(@Size(max = 128) String name, @Size(max = 255) String value, JmsConnectionBE connection) {
        super();
        this.name = name;
        this.value = value;
        this.connection = connection;
    }
}